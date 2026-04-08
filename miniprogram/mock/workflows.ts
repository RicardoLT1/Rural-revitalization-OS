import { ApprovalItem, ProcessDetail, TodoItem, WorkflowNode } from '../types';

export const todoStats = {
  total: 16,
  urgent: 5,
  completedToday: 9
};

export const todoItems: TodoItem[] = [
  { id: 't1', title: '\u9752\u79be\u9a7f\u7ad9\u9662\u843d\u6d41\u8f6c\u534f\u8bae\u786e\u8ba4', dueDate: '\u4eca\u5929 18:00', category: '\u8d44\u4ea7\u6d41\u8f6c', status: '\u5f85\u5904\u7406', processId: 'p1' },
  { id: 't2', title: '\u6625\u8015\u4e3b\u9898\u6d3b\u52a8\u65b9\u6848\u5ba1\u6279', dueDate: '\u4eca\u5929 20:00', category: '\u6d3b\u52a8\u7b79\u5907', status: '\u8fdb\u884c\u4e2d', processId: 'p2' },
  { id: 't3', title: '\u6587\u65c5\u9879\u76ee\u8865\u8d34\u7533\u62a5\u6750\u6599\u590d\u6838', dueDate: '\u660e\u5929 12:00', category: '\u9879\u76ee\u7533\u62a5', status: '\u5f85\u5904\u7406', processId: 'p3' },
  { id: 't4', title: '\u6751\u6c11\u8bae\u4e8b\u8bae\u9898\u6574\u7406', dueDate: '\u6628\u5929 17:30', category: '\u6751\u6c11\u8bae\u4e8b', status: '\u5df2\u903e\u671f', processId: 'p4' }
];

export const approvals: ApprovalItem[] = [
  { id: 'a1', title: '\u53e4\u6865\u5de5\u574a\u6539\u9020\u9884\u7b97\u5ba1\u6279', applicant: '\u674e\u5de5', amount: 18.5, status: '\u5f85\u5ba1\u6279', processId: 'p1', time: '09:20' },
  { id: 'a2', title: '\u6625\u8015\u8282\u7269\u6599\u91c7\u8d2d\u5ba1\u6279', applicant: '\u5468\u7b56\u5212', amount: 6.2, status: '\u5f85\u5ba1\u6279', processId: 'p2', time: '10:05' },
  { id: 'a3', title: '\u7814\u5b66\u8bfe\u7a0b\u8bb2\u5e08\u5916\u8058\u5ba1\u6279', applicant: '\u738b\u8fd0\u8425', amount: 2.8, status: '\u5df2\u9a73\u56de', processId: 'p3', time: '11:40' }
];

export const workflowStrip: WorkflowNode[] = [
  { id: 'n1', name: '\u63d0\u4ea4\u7533\u8bf7', owner: '\u9879\u76ee\u4e13\u5458', status: 'done', time: '04-03 09:10', remark: '\u8d44\u6599\u5df2\u4e0a\u4f20\u3002' },
  { id: 'n2', name: '\u4e61\u9547\u590d\u6838', owner: '\u4e61\u9547\u529e\u516c\u5ba4', status: 'doing', time: '04-04 14:30', remark: '\u9700\u8865\u5145\u9644\u4ef6\u4e8c\u3002' },
  { id: 'n3', name: '\u9884\u7b97\u5ba1\u6279', owner: '\u8d22\u52a1\u7ec4', status: 'pending', time: '-', remark: '\u7b49\u5f85\u590d\u6838\u901a\u8fc7\u3002' },
  { id: 'n4', name: '\u5f52\u6863\u5907\u6848', owner: '\u6863\u6848\u5458', status: 'pending', time: '-', remark: '\u6d41\u7a0b\u5b8c\u6210\u540e\u5f52\u6863\u3002' }
];

export const messages = [
  { id: 'm1', title: '\u6d41\u7a0b p1 \u5728\u201c\u4e61\u9547\u590d\u6838\u201d\u8282\u70b9\u505c\u7559\u8d85\u8fc7 24 \u5c0f\u65f6', time: '5 \u5206\u949f\u524d', processId: 'p1', level: 'warning' },
  { id: 'm2', title: '\u6625\u8015\u6d3b\u52a8\u5ba1\u6279\u5df2\u88ab\u9a73\u56de\uff0c\u8bf7\u8865\u5145\u9884\u7b97\u660e\u7ec6', time: '20 \u5206\u949f\u524d', processId: 'p2', level: 'danger' },
  { id: 'm3', title: '\u5f52\u6863\u4e2d\u5fc3\u65b0\u589e 3 \u6761\u53ef\u5f52\u6863\u8bb0\u5f55', time: '1 \u5c0f\u65f6\u524d', processId: 'p3', level: 'info' }
];

export const processDetails: Record<string, ProcessDetail> = {
  p1: {
    id: 'p1',
    title: '\u9752\u79be\u9a7f\u7ad9\u9662\u843d\u8d44\u4ea7\u6d41\u8f6c',
    status: '\u4e61\u9547\u590d\u6838\u4e2d',
    currentNodeId: 'n2',
    blocker: '\u7b49\u5f85\u4ea7\u6743\u9644\u4ef6\u4e8c\u6b21\u786e\u8ba4',
    nodes: workflowStrip,
    records: [
      { id: 'r1', operator: '\u5f20\u4e13\u5458', action: '\u63d0\u4ea4\u7533\u8bf7', time: '04-03 09:10', remark: '\u9644\u4ef6\u9f50\u5168\uff0c\u63d0\u4ea4\u5ba1\u6279\u3002', nodeId: 'n1' },
      { id: 'r2', operator: '\u5218\u4e3b\u4efb', action: '\u521d\u5ba1\u901a\u8fc7', time: '04-03 15:24', remark: '\u540c\u610f\u8fdb\u5165\u4e61\u9547\u590d\u6838\u3002', nodeId: 'n1' },
      { id: 'r3', operator: '\u4e61\u9547\u529e', action: '\u8865\u4ef6\u901a\u77e5', time: '04-04 14:30', remark: '\u4ea7\u6743\u9644\u4ef6\u4e8c\u9700\u8865\u5145\u76d6\u7ae0\u4ef6\u3002', nodeId: 'n2' }
    ],
    archive: {
      id: 'ar1',
      code: 'QH-2026-0412',
      archivedAt: '\u5f85\u5f52\u6863',
      archivedBy: '\u7cfb\u7edf',
      note: '\u6d41\u7a0b\u5b8c\u6210\u540e\u81ea\u52a8\u751f\u6210\u7559\u75d5\u6863\u6848\u3002'
    }
  },
  p2: {
    id: 'p2',
    title: '\u6625\u8015\u4e3b\u9898\u6d3b\u52a8\u7b79\u5907\u5ba1\u6279',
    status: '\u9884\u7b97\u8865\u5145\u4e2d',
    currentNodeId: 'n2',
    blocker: '\u91c7\u8d2d\u9884\u7b97\u660e\u7ec6\u4e0d\u8db3',
    nodes: workflowStrip,
    records: [
      { id: 'r4', operator: '\u5468\u7b56\u5212', action: '\u63d0\u4ea4\u6d3b\u52a8\u65b9\u6848', time: '04-04 10:00', remark: '\u5df2\u63d0\u4ea4\u521d\u7248\u9884\u7b97\u3002', nodeId: 'n1' },
      { id: 'r5', operator: '\u8d22\u52a1\u7ec4', action: '\u9a73\u56de\u4fee\u6539', time: '04-04 17:36', remark: '\u8bf7\u8865\u5145\u4f9b\u5e94\u5546\u62a5\u4ef7\u4f9d\u636e\u3002', nodeId: 'n2' }
    ],
    archive: {
      id: 'ar2',
      code: 'QH-2026-0413',
      archivedAt: '\u5f85\u5f52\u6863',
      archivedBy: '\u7cfb\u7edf',
      note: '\u9a73\u56de\u8bb0\u5f55\u5df2\u81ea\u52a8\u7559\u75d5\u3002'
    }
  }
};