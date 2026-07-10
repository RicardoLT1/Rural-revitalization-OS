import { isMockMode } from '../config/env';
import { TODO_STATUS_CLASS } from '../constants/status';
import { approvals, messages, processDetails, todoItems, todoStats, workflowStrip } from '../mock/workflows';
import type { OptionItem, StatusType } from '../types/common';
import type {
  CollabWorkbenchView,
  MyApplicationItem,
  OperationLogItem,
  ProcessDetail,
  ProcessRecord,
  RecordFilter,
  TodoItem,
  TodoViewItem
} from '../types/workflow';
import { get, post, request } from '../utils/request';

export const DEFAULT_PROCESS_ID = 'p1';

const categoryOptions: OptionItem[] = [
  { key: '全部', label: '全部' },
  { key: '合作申请', label: '合作申请' }
];

const statusClassMap: Record<string, string> = {
  PENDING: 'pending',
  MATERIAL_REQUIRED: 'pending',
  APPROVED: 'done',
  REJECTED: 'rejected',
  待处理: 'pending',
  进行中: 'processing',
  已逾期: 'overdue',
  已完成: 'done'
};

const statusTextMap: Record<string, string> = {
  PENDING: '待审批',
  MATERIAL_REQUIRED: '待补材料',
  APPROVED: '已通过',
  REJECTED: '已驳回'
};

const enrichTodos = (items: TodoItem[]): TodoViewItem[] => {
  return items.map((item) => ({
    ...item,
    statusClass: statusClassMap[item.status] || TODO_STATUS_CLASS[item.status] || 'pending'
  }));
};

const enrichApplications = (items: MyApplicationItem[]): MyApplicationItem[] => {
  return items.map((item) => ({
    ...item,
    statusText: statusTextMap[item.status] || item.status,
    statusClass: statusClassMap[item.status] || 'pending'
  }));
};

const unwrapList = <T>(payload: unknown): T[] => {
  if (Array.isArray(payload)) {
    return payload as T[];
  }
  const source = payload as Record<string, unknown> | undefined;
  if (!source || typeof source !== 'object') {
    return [];
  }
  if (Array.isArray(source.items)) {
    return source.items as T[];
  }
  if (Array.isArray(source.records)) {
    return source.records as T[];
  }
  if (Array.isArray(source.list)) {
    return source.list as T[];
  }
  if (Array.isArray(source.rows)) {
    return source.rows as T[];
  }
  if (source.data) {
    return unwrapList<T>(source.data);
  }
  return [];
};

export const getCollabWorkbench = async (activeCategory = '全部'): Promise<CollabWorkbenchView> => {
  if (!isMockMode('workflow')) {
    const view = await get<CollabWorkbenchView>('/workflows/workbench', { category: activeCategory });
    return {
      ...view,
      filteredTodos: enrichTodos(view.filteredTodos || [])
    };
  }

  const filteredTodos = activeCategory === '全部'
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
  if (isMockMode('workflow')) {
    return processDetails[id] || processDetails[DEFAULT_PROCESS_ID];
  }
  return get<ProcessDetail>(`/workflows/${id}`);
};

export const getOperationLogs = async (id: string): Promise<OperationLogItem[]> => {
  if (isMockMode('workflow')) {
    return [];
  }
  const payload = await get<unknown>(`/workflows/${id}/operation-logs`);
  return unwrapList<OperationLogItem>(payload);
};

export interface CooperationApplicationRequest {
  resourceId: string;
  title?: string;
  description?: string;
  contactPhone?: string;
  remark?: string;
}

export interface CooperationApplicationResult {
  workflowId: string;
  todoId: string;
  resourceId: string;
  status: 'PENDING';
  created: boolean;
}

export const submitCooperationApplication = async (payload: CooperationApplicationRequest) => {
  return request<CooperationApplicationResult>({
    url: '/workflows/cooperation-applications',
    method: 'POST',
    data: payload,
    header: {
      'Idempotency-Key': createIdempotencyKey()
    }
  });
};

export const getMyApplications = async () => {
  if (isMockMode('workflow')) {
    return [];
  }
  const payload = await get<unknown>('/workflows/my');
  return enrichApplications(unwrapList<MyApplicationItem>(payload));
};

export const approveWorkflow = async (workflowId: string, remark = '') => {
  return post<Record<string, unknown>>(`/workflows/${workflowId}/approve`, { remark });
};

export const rejectWorkflow = async (workflowId: string, remark = '') => {
  return post<Record<string, unknown>>(`/workflows/${workflowId}/reject`, { remark });
};

export const requireWorkflowMaterial = async (workflowId: string, remark = '') => {
  return post<Record<string, unknown>>(`/workflows/processes/${workflowId}/actions`, { action: 'MATERIAL_REQUIRED', remark });
};

export const submitWorkflowMaterials = async (workflowId: string, remark = '') => {
  return post<Record<string, unknown>>(`/workflows/${workflowId}/materials`, { remark });
};

export const getProcessStatusType = (detail: ProcessDetail): StatusType => {
  return detail.blocker ? 'danger' : 'success';
};

export const getProcessRecords = (detail: ProcessDetail, filter: RecordFilter, nodeId: string): ProcessRecord[] => {
  return filter === 'current'
    ? detail.records.filter((item) => item.nodeId === nodeId)
    : detail.records;
};

const createIdempotencyKey = () => {
  const random = Math.random().toString(16).slice(2);
  return `mp-${Date.now()}-${random}`;
};
