import { approvals, messages, todoItems, todoStats, workflowStrip } from '../../mock/workflows';
import { goProcessDetail } from '../../utils/navigation';

Page({
  data: {
    todoStats,
    approvals,
    workflowStrip,
    workflowStripCurrent: workflowStrip.find((item) => item.status === 'doing')?.id || '',
    messages,
    categoryOptions: [
      { key: '全部', label: '全部' },
      { key: '项目申报', label: '项目申报' },
      { key: '资产流转', label: '资产流转' },
      { key: '活动筹备', label: '活动筹备' },
      { key: '村民议事', label: '村民议事' }
    ],
    activeCategory: '全部',
    filteredTodos: todoItems
  },
  onCategoryChange(event: WechatMiniprogram.CustomEvent<{ key: string }>) {
    const activeCategory = event.detail.key;
    const filteredTodos = activeCategory === '全部'
      ? todoItems
      : todoItems.filter((item) => item.category === activeCategory);
    this.setData({ activeCategory, filteredTodos });
  },
  onTodoTap(event: WechatMiniprogram.TouchEvent) {
    const processId = event.currentTarget.dataset.processid;
    goProcessDetail(processId);
  },
  onApprovalTap(event: WechatMiniprogram.CustomEvent<{ processId: string }>) {
    goProcessDetail(event.detail.processId);
  },
  onApprovalAction(event: WechatMiniprogram.CustomEvent<{ action: string }>) {
    wx.showToast({ title: `${event.detail.action === 'pass' ? '已通过' : '已退回'}（演示态）`, icon: 'none' });
  },
  onNodeTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    this.setData({ workflowStripCurrent: event.detail.id });
  },
  onMessageTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    goProcessDetail(event.detail.id);
  }
});
