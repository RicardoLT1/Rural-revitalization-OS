import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getCollabWorkbench } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import { goProcessDetail } from '../../utils/navigation';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u534f\u540c\u5de5\u4f5c\u53f0\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u6682\u65e0\u534f\u540c\u4e8b\u9879',
    emptyDescription: '\u5f53\u524d\u5206\u7c7b\u4e0b\u6ca1\u6709\u5f85\u529e\u3001\u5ba1\u6279\u6216\u63d0\u9192\u6d88\u606f\u3002',
    todoStats: {},
    approvals: [],
    workflowStrip: [],
    workflowStripCurrent: '',
    messages: [],
    categoryOptions: [],
    activeCategory: '\u5168\u90e8',
    filteredTodos: []
  },
  onLoad() {
    this.loadWorkbench(this.data.activeCategory);
  },
  onRetry() {
    this.loadWorkbench(this.data.activeCategory);
  },
  onCategoryChange(event: WechatMiniprogram.CustomEvent<{ key: string }>) {
    this.loadWorkbench(event.detail.key);
  },
  async loadWorkbench(activeCategory: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const view = await getCollabWorkbench(activeCategory);
      const isEmpty = !view.filteredTodos.length && !view.approvals.length && !view.messages.length;
      this.setData({ ...view, pageState: isEmpty ? PageState.Empty : PageState.Ready, isLoading: false });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onTodoTap(event: WechatMiniprogram.TouchEvent) {
    const processId = event.currentTarget.dataset.processid;
    goProcessDetail(processId);
  },
  onApprovalTap(event: WechatMiniprogram.CustomEvent<{ processId: string }>) {
    goProcessDetail(event.detail.processId);
  },
  onApprovalAction(event: WechatMiniprogram.CustomEvent<{ action: string }>) {
    wx.showToast({ title: `${event.detail.action === 'pass' ? '\u5df2\u901a\u8fc7' : '\u5df2\u9a73\u56de'}\uff08\u6f14\u793a\u6001\uff09`, icon: 'none' });
  },
  onNodeTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    this.setData({ workflowStripCurrent: event.detail.id });
  },
  onMessageTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    goProcessDetail(event.detail.id);
  }
});
