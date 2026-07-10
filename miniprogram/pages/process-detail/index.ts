import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { DEFAULT_PROCESS_ID, getOperationLogs, getProcessDetail, submitWorkflowMaterials } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import type { OperationLogItem, ProcessDetail } from '../../types/workflow';

const statusTextMap: Record<string, string> = {
  PENDING: '待审批',
  MATERIAL_REQUIRED: '待补材料',
  APPROVED: '已通过',
  REJECTED: '已驳回'
};

const statusClassMap: Record<string, string> = {
  PENDING: 'pending',
  MATERIAL_REQUIRED: 'pending',
  APPROVED: 'done',
  REJECTED: 'rejected'
};

const actionTextMap: Record<string, string> = {
  SUBMIT_APPLICATION: '提交申请',
  APPROVE_WORKFLOW: '审批处理',
  SUPPLEMENT_MATERIAL: '补充材料'
};

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '申请详情加载失败',
    emptyTitle: '暂无申请详情',
    emptyDescription: '该申请暂未产生处理记录。',
    detail: {} as Partial<ProcessDetail>,
    operationLogs: [] as OperationLogItem[],
    materialRemark: '',
    isSubmittingMaterial: false,
    statusText: '',
    statusClass: 'pending'
  },

  onLoad(query: Record<string, string>) {
    this.loadProcess(query.id || DEFAULT_PROCESS_ID);
  },

  onRetry() {
    this.loadProcess((this.data.detail as Partial<ProcessDetail>).id || DEFAULT_PROCESS_ID);
  },

  async loadProcess(id: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const [detail, operationLogs] = await Promise.all([
        getProcessDetail(id),
        getOperationLogs(id)
      ]);
      this.setData({
        pageState: detail?.id ? PageState.Ready : PageState.Empty,
        isLoading: false,
        detail,
        operationLogs: operationLogs.map((item) => ({
          ...item,
          action: actionTextMap[item.action] || item.action
        })),
        statusText: statusTextMap[detail.status] || detail.status,
        statusClass: statusClassMap[detail.status] || 'pending'
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },

  onMaterialInput(event: WechatMiniprogram.Input) {
    this.setData({ materialRemark: event.detail.value });
  },

  async onSubmitMaterials() {
    const detail = this.data.detail as Partial<ProcessDetail>;
    if (!detail.id || this.data.isSubmittingMaterial) {
      return;
    }
    const remark = this.data.materialRemark.trim() || '已补充材料，请重新审核';
    this.setData({ isSubmittingMaterial: true });
    try {
      await submitWorkflowMaterials(detail.id, remark);
      wx.showToast({ title: '材料已提交', icon: 'success' });
      this.setData({ materialRemark: '' });
      this.loadProcess(detail.id);
    } catch (error) {
      wx.showToast({ title: getErrorMessage(error), icon: 'none' });
    } finally {
      this.setData({ isSubmittingMaterial: false });
    }
  }
});
