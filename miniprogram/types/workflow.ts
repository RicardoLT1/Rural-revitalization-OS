import { OptionItem, StatusType } from './common';

export type WorkflowStatus = 'done' | 'doing' | 'pending' | 'blocked';
export type TodoStatus = '待处理' | '进行中' | '已逾期' | '已完成' | 'PENDING' | 'APPROVED' | 'REJECTED';
export type ApprovalStatus = '待审批' | '已驳回' | '已通过' | 'PENDING' | 'APPROVED' | 'REJECTED';
export type RecordFilter = 'all' | 'current';

export interface TodoStats {
  total: number;
  urgent: number;
  completedToday: number;
}

export interface TodoItem {
  id: string;
  title: string;
  dueDate: string;
  category: string;
  status: TodoStatus;
  processId: string;
}

export interface TodoViewItem extends TodoItem {
  statusClass: string;
}

export interface ApprovalItem {
  id: string;
  title: string;
  applicant: string;
  amount: number;
  status: ApprovalStatus;
  processId: string;
  time: string;
}

export interface MyApplicationItem {
  id: string;
  title: string;
  category: string;
  resourceId: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | string;
  currentNodeId: string;
  createdAt: string;
  remark?: string;
  handledAt?: string;
  statusText?: string;
  statusClass?: string;
}

export interface WorkflowNode {
  id: string;
  name: string;
  owner: string;
  status: WorkflowStatus;
  time: string;
  remark: string;
}

export type WorkflowTimelineItem = WorkflowNode;

export interface ProcessRecord {
  id?: string;
  operator?: string;
  action: string;
  time?: string;
  remark: string;
  nodeId: string;
}

export interface OperationLogItem {
  id: string;
  workflowId: string;
  resourceId?: string;
  action: string;
  operatorId?: string;
  operatorName?: string;
  remark?: string;
  createdAt: string;
}

export interface ArchiveRecord {
  id: string;
  code: string;
  archivedAt: string;
  archivedBy: string;
  note: string;
}

export interface WorkflowDetail {
  id: string;
  title: string;
  status: string;
  currentNodeId: string;
  blocker?: string;
  applicantName?: string;
  nodes: WorkflowNode[];
  records: ProcessRecord[];
  archive?: ArchiveRecord;
}

export type ProcessDetail = WorkflowDetail;

export interface WorkflowMessage {
  id: string;
  title: string;
  time: string;
  processId: string;
  level: StatusType | 'warning' | 'danger';
}

export interface CollabWorkbenchView {
  todoStats: TodoStats;
  approvals: ApprovalItem[];
  workflowStrip: WorkflowNode[];
  workflowStripCurrent: string;
  messages: WorkflowMessage[];
  categoryOptions: OptionItem[];
  activeCategory: string;
  filteredTodos: TodoViewItem[];
}
