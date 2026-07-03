const { getMyApplications } = require('../../services/workflow');
const { goProcessDetail } = require('../../utils/navigation');

const PageState = {
  Loading: 'loading',
  Ready: 'ready',
  Empty: 'empty',
  Error: 'error'
};

const statusOptions = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING', label: '待审批' },
  { key: 'APPROVED', label: '已通过' },
  { key: 'REJECTED', label: '已驳回' }
];

const getErrorMessage = (error) => error && error.message ? error.message : '请求失败，请稍后重试';

Page({
  data: {
    pageState: PageState.Loading,
    isLoading: true,
    errorMessage: '',
    loadingText: '加载中...',
    errorTitle: '我的申请加载失败',
    emptyTitle: '暂无合作申请',
    emptyDescription: '在资源详情页提交合作申请后，可在这里查看审批进度。',
    statusOptions,
    activeStatus: 'ALL',
    allApplications: [],
    myApplications: []
  },

  onLoad() {
    this.loadApplications();
  },

  onShow() {
    if (!this.data.isLoading) {
      this.loadApplications();
    }
  },

  onPullDownRefresh() {
    this.loadApplications().finally(() => wx.stopPullDownRefresh());
  },

  onRetry() {
    this.loadApplications();
  },

  onStatusTap(event) {
    const value = event.currentTarget.dataset.value;
    if (value) {
      this.setData({ activeStatus: value }, () => this.applyFilter());
    }
  },

  async loadApplications() {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const list = await getMyApplications();
      this.setData({ allApplications: Array.isArray(list) ? list : [], isLoading: false }, () => this.applyFilter());
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },

  applyFilter() {
    const activeStatus = this.data.activeStatus;
    const source = Array.isArray(this.data.allApplications) ? this.data.allApplications : [];
    const myApplications = activeStatus === 'ALL'
      ? source
      : source.filter((item) => item.status === activeStatus);
    this.setData({
      myApplications,
      pageState: myApplications.length ? PageState.Ready : PageState.Empty
    });
  },

  onApplicationTap(event) {
    const processId = event.currentTarget.dataset.processid;
    if (processId) {
      goProcessDetail(processId);
    }
  }
});