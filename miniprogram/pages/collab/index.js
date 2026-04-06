const { approvals, messages, todoItems, todoStats, workflowStrip } = require('../../mock/workflows');
const { goProcessDetail } = require('../../utils/navigation');

const statusClassMap = {
  待处理: 'pending',
  进行中: 'processing',
  已逾期: 'overdue'
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
      { key: '全部', label: '全部' },
      { key: '项目申报', label: '项目申报' },
      { key: '资产流转', label: '资产流转' },
      { key: '活动筹备', label: '活动筹备' },
      { key: '村民议事', label: '村民议事' }
    ],
    activeCategory: '全部',
    filteredTodos: enrichTodos(todoItems)
  },
  onCategoryChange(event) {
    const activeCategory = event.detail.key;
    const filteredTodos = activeCategory === '全部'
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
    wx.showToast({ title: `${event.detail.action === 'pass' ? '已通过' : '已退回'}（演示态）`, icon: 'none' });
  },
  onNodeTap(event) {
    this.setData({ workflowStripCurrent: event.detail.id });
  },
  onMessageTap(event) {
    goProcessDetail(event.detail.id);
  }
});