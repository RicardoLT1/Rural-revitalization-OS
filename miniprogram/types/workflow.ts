import { OptionItem, StatusType } from './common';

export type WorkflowStatus = 'done' | 'doing' | 'pending' | 'blocked';
export type TodoStatus = '\u5f85\u5904\u7406' | '\u8fdb\u884c\u4e2d' | '\u5df2\u903e\u671f' | '\u5df2\u5b8c\u6210';
export type ApprovalStatus = '\u5f85\u5ba1\u6279' | '\u5df2\u9a73\u56de' | '\u5df2\u901a\u8fc7';
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
  id: string;
  operator: string;
  action: string;
  time: string;
  remark: string;
  nodeId: string;
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
  blocker: string;
  nodes: WorkflowNode[];
  records: ProcessRecord[];
  archive: ArchiveRecord;
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