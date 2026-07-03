const { getResourceTags, getResources } = require('../../services/resource');
const { goResourceDetail } = require('../../utils/navigation');

const PageState = { Loading: 'loading', Ready: 'ready', Empty: 'empty', Error: 'error' };
const PAGE_SIZE = 10;
const statusOptions = ['全部', '可招商', '洽谈中', '已签约'];
const getErrorMessage = (error) => error && error.message ? error.message : '资源列表加载失败，请稍后重试';
const safeArray = (value) => Array.isArray(value) ? value : [];

Page({
  data: {
    pageState: PageState.Loading,
    isLoading: true,
    isRefreshing: false,
    isLoadingMore: false,
    hasMore: true,
    errorMessage: '',
    loadingText: '正在加载乡耘 OS 数据...',
    errorTitle: '资源列表加载失败',
    emptyTitle: '暂无匹配资源',
    emptyDescription: '请调整关键词、分类或状态后再试。',
    keyword: '',
    categoryOptions: ['全部'],
    statusOptions,
    activeCategory: '全部',
    activeStatus: '全部',
    page: 1,
    resources: [],
    mapCenter: { latitude: 30.2239, longitude: 120.1661 },
    markers: []
  },

  onLoad() {
    this.initFilters();
    this.loadResources(true);
  },

  onPullDownRefresh() {
    this.setData({ isRefreshing: true });
    this.loadResources(true).finally(() => {
      this.setData({ isRefreshing: false });
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.isLoadingMore) {
      this.loadResources(false);
    }
  },

  onRetry() {
    this.loadResources(true);
  },

  async initFilters() {
    try {
      const tags = safeArray(await getResourceTags());
      this.setData({ categoryOptions: ['全部'].concat(tags.filter((item) => item && item !== '全部')) });
    } catch (error) {
      this.setData({ categoryOptions: ['全部'] });
    }
  },

  onKeywordInput(event) {
    this.setData({ keyword: event.detail.value });
  },

  onSearch() {
    this.loadResources(true);
  },

  onCategoryTap(event) {
    const value = event.currentTarget.dataset.value;
    if (value && value !== this.data.activeCategory) {
      this.setData({ activeCategory: value });
      this.loadResources(true);
    }
  },

  onStatusTap(event) {
    const value = event.currentTarget.dataset.value;
    if (value && value !== this.data.activeStatus) {
      this.setData({ activeStatus: value });
      this.loadResources(true);
    }
  },

  async loadResources(reset) {
    const nextPage = reset ? 1 : this.data.page + 1;
    this.setData({
      pageState: reset ? PageState.Loading : this.data.pageState,
      isLoading: reset,
      isLoadingMore: !reset,
      errorMessage: ''
    });
    try {
      const list = safeArray(await getResources({
        page: nextPage,
        size: PAGE_SIZE,
        keyword: this.data.keyword.trim(),
        category: this.data.activeCategory,
        status: this.data.activeStatus
      }));
      const resources = reset ? list : this.data.resources.concat(list);
      this.setData({
        resources,
        page: nextPage,
        hasMore: list.length >= PAGE_SIZE,
        markers: this.toMarkers(resources),
        pageState: resources.length ? PageState.Ready : PageState.Empty,
        isLoading: false,
        isLoadingMore: false
      });
    } catch (error) {
      const message = getErrorMessage(error);
      this.setData({
        pageState: reset ? PageState.Error : this.data.pageState,
        isLoading: false,
        isLoadingMore: false,
        errorMessage: message
      });
      if (!reset) wx.showToast({ title: message, icon: 'none' });
    }
  },

  onResourceTap(event) {
    const id = event.currentTarget.dataset.id;
    if (id) goResourceDetail(id);
  },

  toMarkers(resources) {
    return safeArray(resources)
      .filter((item) => item.lat && item.lng)
      .map((item, index) => ({
        id: index + 1,
        latitude: Number(item.lat),
        longitude: Number(item.lng),
        title: item.name,
        width: 28,
        height: 28
      }));
  }
});