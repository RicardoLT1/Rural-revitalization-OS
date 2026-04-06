const { processDetails } = require('../../mock/workflows');

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
  onLoad(query) {
    const id = query.id || 'p1';
    const detail = processDetails[id] || processDetails.p1;
    const statusType = detail.blocker ? 'danger' : 'success';
    this.setData({
      detail,
      statusType,
      visibleRecords: detail.records
    });
  },
  onNodeTap(event) {
    this.setData({
      detail: {
        ...this.data.detail,
        currentNodeId: event.detail.id
      }
    });
    this.applyRecordFilter(this.data.recordFilter, event.detail.id);
  },
  onNodeToggle(event) {
    const id = event.detail.id;
    this.setData({ expandedNodeId: this.data.expandedNodeId === id ? '' : id });
  },
  onRecordFilterChange(event) {
    const recordFilter = event.detail.key;
    this.setData({ recordFilter });
    this.applyRecordFilter(recordFilter, this.data.detail.currentNodeId);
  },
  applyRecordFilter(filter, nodeId) {
    const visibleRecords = filter === 'current'
      ? this.data.detail.records.filter((item) => item.nodeId === nodeId)
      : this.data.detail.records;
    this.setData({ visibleRecords });
  }
});