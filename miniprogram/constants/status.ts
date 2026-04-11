import type { StatusType } from '../types/common';

export const INVESTMENT_STATUS_TYPE: Record<string, StatusType> = {
  '\u53ef\u62db\u5546': 'success',
  '\u6d3d\u8c08\u4e2d': 'warning',
  '\u5df2\u7b7e\u7ea6': 'info'
};

export const PROCESS_STATUS_TYPE: Record<string, StatusType> = {
  done: 'success',
  doing: 'info',
  pending: 'warning',
  blocked: 'danger'
};

export const TODO_STATUS_CLASS: Record<string, string> = {
  '\u5f85\u5904\u7406': 'pending',
  '\u8fdb\u884c\u4e2d': 'processing',
  '\u5df2\u903e\u671f': 'overdue',
  '\u5df2\u5b8c\u6210': 'done'
};

export const PRIORITY_STATUS_TYPE: Record<string, StatusType> = {
  '\u9ad8\u4f18\u5148': 'danger',
  '\u4e2d\u4f18\u5148': 'warning',
  '\u89c2\u5bdf': 'info'
};
