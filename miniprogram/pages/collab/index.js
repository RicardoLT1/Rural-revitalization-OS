const { getCollabWorkbench } = require('../../services/workflow');
const { goProcessDetail } = require('../../utils/navigation');

Page({
  data: {
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
  onCategoryChange(event) {
    this.loadWorkbench(event.detail.key);
  },
  async loadWorkbench(activeCategory) {
    const view = await getCollabWorkbench(activeCategory);
    this.setData(view);
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
