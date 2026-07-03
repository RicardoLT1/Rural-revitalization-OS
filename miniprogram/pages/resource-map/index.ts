import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getResourceTags, getResources } from '../../services/resource';
import type { PageLoadState } from '../../types/common';
import type { ResourcePoint } from '../../types/resource';
import { goResourceDetail } from '../../utils/navigation';

const PAGE_SIZE = 10;
const statusOptions = ['全部', '可招商', '洽谈中', '已签约'];

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    isRefreshing: false,
    isLoadingMore: false,
    hasMore: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '资源列表加载失败',
    emptyTitle: '暂无匹配资源',
    emptyDescription: '请调整关键词、分类或状态后再试。',
    keyword: '',
    categoryOptions: ['全部'] as string[],
    statusOptions,
    activeCategory: '全部',
    activeStatus: '全部',
    page: 1,
    resources: [] as ResourcePoint[],
    mapCenter: { latitude: 30.2239, longitude: 120.1661 },
    markers: [] as WechatMiniprogram.MapMarker[]
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
      const tags = await getResourceTags();
      this.setData({ categoryOptions: ['全部', ...tags.filter((item) => item !== '全部')] });
    } catch (error) {
      this.setData({ categoryOptions: ['全部'] });
    }
  },

  onKeywordInput(event: WechatMiniprogram.Input) {
    this.setData({ keyword: event.detail.value });
  },

  onSearch() {
    this.loadResources(true);
  },

  onCategoryTap(event: WechatMiniprogram.TouchEvent) {
    const value = event.currentTarget.dataset.value;
    if (value && value !== this.data.activeCategory) {
      this.setData({ activeCategory: value });
      this.loadResources(true);
    }
  },

  onStatusTap(event: WechatMiniprogram.TouchEvent) {
    const value = event.currentTarget.dataset.value;
    if (value && value !== this.data.activeStatus) {
      this.setData({ activeStatus: value });
      this.loadResources(true);
    }
  },

  async loadResources(reset: boolean) {
    const nextPage = reset ? 1 : this.data.page + 1;
    this.setData({
      pageState: reset ? PageState.Loading : this.data.pageState,
      isLoading: reset,
      isLoadingMore: !reset,
      errorMessage: ''
    });
    try {
      const list = await getResources({
        page: nextPage,
        size: PAGE_SIZE,
        keyword: this.data.keyword.trim(),
        category: this.data.activeCategory,
        status: this.data.activeStatus
      });
      const resources = reset ? list : [...this.data.resources, ...list];
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
      this.setData({
        pageState: reset ? PageState.Error : this.data.pageState,
        isLoading: false,
        isLoadingMore: false,
        errorMessage: getErrorMessage(error)
      });
      if (!reset) {
        wx.showToast({ title: getErrorMessage(error), icon: 'none' });
      }
    }
  },

  onResourceTap(event: WechatMiniprogram.TouchEvent) {
    const id = event.currentTarget.dataset.id;
    if (id) {
      goResourceDetail(id);
    }
  },

  toMarkers(resources: ResourcePoint[]): WechatMiniprogram.MapMarker[] {
    return resources
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
