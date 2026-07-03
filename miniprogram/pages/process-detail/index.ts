import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { DEFAULT_PROCESS_ID, getProcessDetail } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import type { ProcessDetail } from '../../types/workflow';

const statusTextMap: Record<string, string> = {
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '已驳回'
};

const statusClassMap: Record<string, string> = {
  PENDING: 'pending',
  APPROVED: 'done',
  REJECTED: 'rejected'
};

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '申请详情加载失败',
    emptyTitle: '暂无申请详情',
    emptyDescription: '该申请暂未产生处理记录。',
    detail: {} as Partial<ProcessDetail>,
    statusText: '',
    statusClass: 'pending'
  },

  onLoad(query: Record<string, string>) {
    this.loadProcess(query.id || DEFAULT_PROCESS_ID);
  },

  onRetry() {
    this.loadProcess((this.data.detail as Partial<ProcessDetail>).id || DEFAULT_PROCESS_ID);
  },

  async loadProcess(id: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const detail = await getProcessDetail(id);
      this.setData({
        pageState: detail?.id ? PageState.Ready : PageState.Empty,
        isLoading: false,
        detail,
        statusText: statusTextMap[detail.status] || detail.status,
        statusClass: statusClassMap[detail.status] || 'pending'
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  }
});
