import { isMockMode } from '../config/env';
import { TODO_STATUS_CLASS } from '../constants/status';
import { approvals, messages, processDetails, todoItems, todoStats, workflowStrip } from '../mock/workflows';
import type { OptionItem, StatusType } from '../types/common';
import type { CollabWorkbenchView, ProcessDetail, ProcessRecord, RecordFilter, TodoItem, TodoViewItem } from '../types/workflow';
import { get } from '../utils/request';

export const DEFAULT_PROCESS_ID = 'p1';

const categoryOptions: OptionItem[] = [
  { key: '\u5168\u90e8', label: '\u5168\u90e8' },
  { key: '\u9879\u76ee\u7533\u62a5', label: '\u9879\u76ee\u7533\u62a5' },
  { key: '\u8d44\u4ea7\u6d41\u8f6c', label: '\u8d44\u4ea7\u6d41\u8f6c' },
  { key: '\u6d3b\u52a8\u7b79\u5907', label: '\u6d3b\u52a8\u7b79\u5907' },
  { key: '\u6751\u6c11\u8bae\u4e8b', label: '\u6751\u6c11\u8bae\u4e8b' }
];

const enrichTodos = (items: TodoItem[]): TodoViewItem[] => {
  return items.map((item) => ({
    ...item,
    statusClass: TODO_STATUS_CLASS[item.status] || 'pending'
  }));
};

export const getCollabWorkbench = async (activeCategory = '\u5168\u90e8'): Promise<CollabWorkbenchView> => {
  if (!isMockMode()) {
    return get<CollabWorkbenchView>('/workflows/workbench', { category: activeCategory });
  }

  const filteredTodos = activeCategory === '\u5168\u90e8'
    ? todoItems
    : todoItems.filter((item) => item.category === activeCategory);

  return {
    todoStats,
    approvals,
    workflowStrip,
    workflowStripCurrent: workflowStrip.find((item) => item.status === 'doing')?.id || '',
    messages,
    categoryOptions,
    activeCategory,
    filteredTodos: enrichTodos(filteredTodos)
  };
};

export const getProcessDetail = async (id = DEFAULT_PROCESS_ID): Promise<ProcessDetail> => {
  if (isMockMode()) {
    return processDetails[id] || processDetails[DEFAULT_PROCESS_ID];
  }
  return get<ProcessDetail>(`/workflows/processes/${id}`);
};

export const getProcessStatusType = (detail: ProcessDetail): StatusType => {
  return detail.blocker ? 'danger' : 'success';
};

export const getProcessRecords = (detail: ProcessDetail, filter: RecordFilter, nodeId: string): ProcessRecord[] => {
  return filter === 'current'
    ? detail.records.filter((item) => item.nodeId === nodeId)
    : detail.records;
};
