const { approvals, messages, todoItems, todoStats, workflowStrip } = require('../../mock/workflows');
const { goProcessDetail } = require('../../utils/navigation');

const statusClassMap = {
  '\u5f85\u5904\u7406': 'pending',
  '\u8fdb\u884c\u4e2d': 'processing',
  '\u5df2\u903e\u671f': 'overdue'
};

const enrichTodos = (items) => items.map((item) => ({
  ...item,
  statusClass: statusClassMap[item.status] || 'pending'
}));

Page({
  data: {
    todoStats,
    approvals,
    workflowStrip,
    workflowStripCurrent: (workflowStrip.find((item) => item.status === 'doing') || {}).id || '',
    messages,
    categoryOptions: [
      { key: '\u5168\u90e8', label: '\u5168\u90e8' },
      { key: '\u9879\u76ee\u7533\u62a5', label: '\u9879\u76ee\u7533\u62a5' },
      { key: '\u8d44\u4ea7\u6d41\u8f6c', label: '\u8d44\u4ea7\u6d41\u8f6c' },
      { key: '\u6d3b\u52a8\u7b79\u5907', label: '\u6d3b\u52a8\u7b79\u5907' },
      { key: '\u6751\u6c11\u8bae\u4e8b', label: '\u6751\u6c11\u8bae\u4e8b' }
    ],
    activeCategory: '\u5168\u90e8',
    filteredTodos: enrichTodos(todoItems)
  },
  onCategoryChange(event) {
    const activeCategory = event.detail.key;
    const filteredTodos = activeCategory === '\u5168\u90e8'
      ? todoItems
      : todoItems.filter((item) => item.category === activeCategory);
    this.setData({ activeCategory, filteredTodos: enrichTodos(filteredTodos) });
  },
  onTodoTap(event) {
    const processId = event.currentTarget.dataset.processid;
    goProcessDetail(processId);
  },
  onApprovalTap(event) {
    goProcessDetail(event.detail.processId);
  },
  onApprovalAction(event) {
    wx.showToast({ title: `${event.detail.action === 'pass' ? '\u5df2\u901a\u8fc7' : '\u5df2\u9a73\u56de'}\uff08\u6f14\u793a\u6001\uff09`, icon: 'none' });
  },
  onNodeTap(event) {
    this.setData({ workflowStripCurrent: event.detail.id });
  },
  onMessageTap(event) {
    goProcessDetail(event.detail.id);
  }
});