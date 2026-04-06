import { ApprovalItem, ProcessDetail, TodoItem, WorkflowNode } from '../types';

export const todoStats = {
  total: 16,
  urgent: 5,
  completedToday: 9
};

export const todoItems: TodoItem[] = [
  { id: 't1', title: '青禾驿站院落流转协议确认', dueDate: '今天 18:00', category: '资产流转', status: '待处理', processId: 'p1' },
  { id: 't2', title: '春耕主题活动方案审批', dueDate: '今天 20:00', category: '活动筹备', status: '进行中', processId: 'p2' },
  { id: 't3', title: '文旅项目补贴申报材料复核', dueDate: '明天 12:00', category: '项目申报', status: '待处理', processId: 'p3' },
  { id: 't4', title: '村民议事议题整理', dueDate: '昨天 17:30', category: '村民议事', status: '已逾期', processId: 'p4' }
];

export const approvals: ApprovalItem[] = [
  { id: 'a1', title: '古桥工坊改造预算审批', applicant: '李工', amount: 18.5, status: '待审批', processId: 'p1', time: '09:20' },
  { id: 'a2', title: '春耕节物料采购审批', applicant: '周策划', amount: 6.2, status: '待审批', processId: 'p2', time: '10:05' },
  { id: 'a3', title: '研学课程讲师外聘审批', applicant: '王运营', amount: 2.8, status: '已退回', processId: 'p3', time: '11:40' }
];

export const workflowStrip: WorkflowNode[] = [
  { id: 'n1', name: '提交申请', owner: '项目专员', status: 'done', time: '04-03 09:10', remark: '资料已上传' },
  { id: 'n2', name: '乡镇复核', owner: '乡镇办公室', status: 'doing', time: '04-04 14:30', remark: '需补充附件2' },
  { id: 'n3', name: '预算审批', owner: '财务组', status: 'pending', time: '-', remark: '等待复核通过' },
  { id: 'n4', name: '归档备案', owner: '档案员', status: 'pending', time: '-', remark: '流程完成后归档' }
];

export const messages = [
  { id: 'm1', title: '流程 p1 在“乡镇复核”节点停留超过 24 小时', time: '5 分钟前', processId: 'p1', level: 'warning' },
  { id: 'm2', title: '春耕活动审批已被退回，请补充预算明细', time: '20 分钟前', processId: 'p2', level: 'danger' },
  { id: 'm3', title: '归档中心新增 3 条可归档记录', time: '1 小时前', processId: 'p3', level: 'info' }
];

export const processDetails: Record<string, ProcessDetail> = {
  p1: {
    id: 'p1',
    title: '青禾驿站院落资产流转',
    status: '乡镇复核中',
    currentNodeId: 'n2',
    blocker: '等待产权附件二次确认',
    nodes: workflowStrip,
    records: [
      { id: 'r1', operator: '张专员', action: '提交申请', time: '04-03 09:10', remark: '附件齐全，提交审批。', nodeId: 'n1' },
      { id: 'r2', operator: '刘主任', action: '初审通过', time: '04-03 15:24', remark: '同意进入乡镇复核。', nodeId: 'n1' },
      { id: 'r3', operator: '乡镇办', action: '补件通知', time: '04-04 14:30', remark: '产权附件2需补充盖章件。', nodeId: 'n2' }
    ],
    archive: {
      id: 'ar1',
      code: 'QH-2026-0412',
      archivedAt: '待归档',
      archivedBy: '系统',
      note: '流程完成后自动生成留痕档案。'
    }
  },
  p2: {
    id: 'p2',
    title: '春耕主题活动筹备审批',
    status: '预算补充中',
    currentNodeId: 'n2',
    blocker: '采购预算明细不足',
    nodes: workflowStrip,
    records: [
      { id: 'r4', operator: '周策划', action: '提交活动方案', time: '04-04 10:00', remark: '已提交初版预算。', nodeId: 'n1' },
      { id: 'r5', operator: '财务组', action: '退回修改', time: '04-04 17:36', remark: '请补充供应商报价依据。', nodeId: 'n2' }
    ],
    archive: {
      id: 'ar2',
      code: 'QH-2026-0413',
      archivedAt: '待归档',
      archivedBy: '系统',
      note: '退回记录已自动留痕。'
    }
  }
};
