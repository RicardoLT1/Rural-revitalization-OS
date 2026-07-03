import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getInvestmentStatusType, getResourceDetail } from '../../services/resource';
import { submitCooperationApplication } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import type { ResourceDetail } from '../../types/resource';

const PHONE_REG = /^1\d{10}$/;
const canSubmitApplication = (status = '') => {
  const value = String(status).toUpperCase();
  if (!value) {
    return true;
  }
  return !['已签约', '下架', '不可用', 'CLOSED', 'SIGNED', 'OFFLINE'].some((item) => value.includes(item.toUpperCase()));
};

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    isSubmittingApplication: false,
    showApplyForm: false,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '资源详情加载失败',
    emptyTitle: '未找到资源',
    emptyDescription: '该资源可能已下架或暂未入库。',
    detail: {} as Partial<ResourceDetail>,
    statusType: 'success',
    canApply: false,
    cannotApplyReason: '',
    form: {
      title: '',
      description: '',
      contactPhone: ''
    }
  },

  onLoad(query: Record<string, string>) {
    this.loadDetail(query.id || '101');
  },

  onRetry() {
    this.loadDetail((this.data.detail as Partial<ResourceDetail>).id || '101');
  },

  async loadDetail(id: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const detail = await getResourceDetail(id);
      const canApply = canSubmitApplication(detail.investmentStatus);
      this.setData({
        pageState: detail?.id ? PageState.Ready : PageState.Empty,
        isLoading: false,
        detail,
        statusType: getInvestmentStatusType(detail.investmentStatus),
        canApply,
        cannotApplyReason: canApply ? '' : '该资源当前状态不可提交合作申请',
        form: {
          ...this.data.form,
          title: `${detail.name || '资源'}合作申请`
        }
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },

  onShowApplyForm() {
    if (!this.data.canApply) {
      wx.showToast({ title: this.data.cannotApplyReason, icon: 'none' });
      return;
    }
    this.setData({ showApplyForm: true });
  },

  onCloseApplyForm() {
    if (!this.data.isSubmittingApplication) {
      this.setData({ showApplyForm: false });
    }
  },

  onFormInput(event: WechatMiniprogram.Input) {
    const key = event.currentTarget.dataset.key;
    if (key) {
      this.setData({ [`form.${key}`]: event.detail.value });
    }
  },

  async onSubmitApplication() {
    const detail = this.data.detail as Partial<ResourceDetail>;
    const form = this.data.form;
    if (!detail.id || this.data.isSubmittingApplication) {
      return;
    }
    if (!form.title.trim()) {
      wx.showToast({ title: '请填写申请标题', icon: 'none' });
      return;
    }
    if (form.description.trim().length < 10) {
      wx.showToast({ title: '合作说明至少 10 个字', icon: 'none' });
      return;
    }
    if (!PHONE_REG.test(form.contactPhone.trim())) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' });
      return;
    }

    this.setData({ isSubmittingApplication: true });
    try {
      const result = await submitCooperationApplication({
        resourceId: detail.id,
        title: form.title.trim(),
        description: form.description.trim(),
        contactPhone: form.contactPhone.trim()
      });
      wx.showModal({
        title: result.created ? '申请已提交' : '申请已存在',
        content: `申请编号：${result.workflowId}`,
        showCancel: false,
        success: () => wx.switchTab({ url: '/pages/collab/index' })
      });
    } catch (error) {
      wx.showToast({ title: getErrorMessage(error), icon: 'none' });
    } finally {
      this.setData({ isSubmittingApplication: false });
    }
  },

  onContact() {
    wx.showToast({ title: '可在提交申请后等待工作人员对接', icon: 'none' });
  }
});
