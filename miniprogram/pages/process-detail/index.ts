import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { DEFAULT_PROCESS_ID, getProcessDetail, getProcessRecords, getProcessStatusType } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import type { ProcessDetail, RecordFilter } from '../../types/workflow';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u6d41\u7a0b\u8be6\u60c5\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u6682\u65e0\u6d41\u7a0b\u8bb0\u5f55',
    emptyDescription: '\u8be5\u6d41\u7a0b\u6682\u65e0\u8282\u70b9\u6216\u5904\u7406\u7559\u75d5\u3002',
    detail: {} as Partial<ProcessDetail>,
    statusType: 'warning',
    expandedNodeId: '',
    recordFilters: [
      { key: 'all', label: '\u5168\u90e8\u8bb0\u5f55' },
      { key: 'current', label: '\u5f53\u524d\u8282\u70b9\u8bb0\u5f55' }
    ],
    recordFilter: 'all',
    visibleRecords: []
  },
  onLoad(query: Record<string, string>) {
    const id = query.id || DEFAULT_PROCESS_ID;
    this.loadProcess(id);
  },
  onRetry() {
    this.loadProcess((this.data.detail as Partial<ProcessDetail>).id || DEFAULT_PROCESS_ID);
  },
  async loadProcess(id: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const detail = await getProcessDetail(id);
      const isEmpty = !detail.nodes.length && !detail.records.length;
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        detail,
        statusType: getProcessStatusType(detail),
        visibleRecords: detail.records
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onNodeTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    this.setData({
      detail: {
        ...this.data.detail,
        currentNodeId: event.detail.id
      }
    });
    this.applyRecordFilter(this.data.recordFilter as RecordFilter, event.detail.id);
  },
  onNodeToggle(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    const id = event.detail.id;
    this.setData({ expandedNodeId: this.data.expandedNodeId === id ? '' : id });
  },
  onRecordFilterChange(event: WechatMiniprogram.CustomEvent<{ key: RecordFilter }>) {
    const recordFilter = event.detail.key;
    this.setData({ recordFilter });
    this.applyRecordFilter(recordFilter, (this.data.detail as Partial<ProcessDetail>).currentNodeId || '');
  },
  applyRecordFilter(filter: RecordFilter, nodeId: string) {
    const detail = this.data.detail as ProcessDetail;
    if (!detail.records) {
      this.setData({ visibleRecords: [] });
      return;
    }
    this.setData({ visibleRecords: getProcessRecords(detail, filter, nodeId) });
  }
});
