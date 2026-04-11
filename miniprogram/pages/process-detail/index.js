const { DEFAULT_PROCESS_ID, getProcessDetail, getProcessRecords, getProcessStatusType } = require('../../services/workflow');

Page({
  data: {
    detail: {},
    statusType: 'warning',
    expandedNodeId: '',
    recordFilters: [
      { key: 'all', label: '\u5168\u90e8\u8bb0\u5f55' },
      { key: 'current', label: '\u5f53\u524d\u8282\u70b9\u8bb0\u5f55' }
    ],
    recordFilter: 'all',
    visibleRecords: []
  },
  onLoad(query) {
    const id = query.id || DEFAULT_PROCESS_ID;
    this.loadProcess(id);
  },
  async loadProcess(id) {
    const detail = await getProcessDetail(id);
    this.setData({
      detail,
      statusType: getProcessStatusType(detail),
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
    const visibleRecords = getProcessRecords(this.data.detail, filter, nodeId);
    this.setData({ visibleRecords });
  }
});
