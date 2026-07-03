import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getMyApplications } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import type { MyApplicationItem } from '../../types/workflow';
import { goProcessDetail } from '../../utils/navigation';

const statusOptions = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING', label: '待审批' },
  { key: 'APPROVED', label: '已通过' },
  { key: 'REJECTED', label: '已驳回' }
];

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '我的申请加载失败',
    emptyTitle: '暂无合作申请',
    emptyDescription: '在资源详情页提交合作申请后，可在这里查看审批进度。',
    statusOptions,
    activeStatus: 'ALL',
    allApplications: [] as MyApplicationItem[],
    myApplications: [] as MyApplicationItem[]
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

  onStatusTap(event: WechatMiniprogram.TouchEvent) {
    const value = event.currentTarget.dataset.value;
    if (value) {
      this.setData({ activeStatus: value }, () => this.applyFilter());
    }
  },

  async loadApplications() {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const list = await getMyApplications();
      this.setData({ allApplications: list, isLoading: false }, () => this.applyFilter());
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },

  applyFilter() {
    const activeStatus = this.data.activeStatus;
    const myApplications = activeStatus === 'ALL'
      ? this.data.allApplications
      : this.data.allApplications.filter((item) => item.status === activeStatus);
    this.setData({
      myApplications,
      pageState: myApplications.length ? PageState.Ready : PageState.Empty
    });
  },

  onApplicationTap(event: WechatMiniprogram.TouchEvent) {
    const processId = event.currentTarget.dataset.processid;
    if (processId) {
      goProcessDetail(processId);
    }
  }
});
