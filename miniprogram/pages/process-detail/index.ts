import { processDetails } from '../../mock/workflows';

Page({
  data: {
    detail: processDetails.p1,
    statusType: 'warning',
    expandedNodeId: '',
    recordFilters: [
      { key: 'all', label: '全部记录' },
      { key: 'current', label: '当前节点记录' }
    ],
    recordFilter: 'all',
    visibleRecords: processDetails.p1.records
  },
  onLoad(query: Record<string, string>) {
    const id = query.id || 'p1';
    const detail = processDetails[id] || processDetails.p1;
    const statusType = detail.blocker ? 'danger' : 'success';
    this.setData({
      detail,
      statusType,
      visibleRecords: detail.records
    });
  },
  onNodeTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    this.setData({
      detail: {
        ...this.data.detail,
        currentNodeId: event.detail.id
      }
    });
    this.applyRecordFilter(this.data.recordFilter, event.detail.id);
  },
  onNodeToggle(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    const id = event.detail.id;
    this.setData({ expandedNodeId: this.data.expandedNodeId === id ? '' : id });
  },
  onRecordFilterChange(event: WechatMiniprogram.CustomEvent<{ key: 'all' | 'current' }>) {
    const recordFilter = event.detail.key;
    this.setData({ recordFilter });
    this.applyRecordFilter(recordFilter, this.data.detail.currentNodeId);
  },
  applyRecordFilter(filter: 'all' | 'current', nodeId: string) {
    const visibleRecords = filter === 'current'
      ? this.data.detail.records.filter((item) => item.nodeId === nodeId)
      : this.data.detail.records;
    this.setData({ visibleRecords });
  }
});
