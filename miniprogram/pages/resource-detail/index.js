const { getResourceDetail, getInvestmentStatusType } = require('../../services/resource');
const { submitCooperationApplication } = require('../../services/workflow');

const PHONE_REG = /^1\d{10}$/;
const canSubmitApplication = (status = '') => {
  const value = String(status).toUpperCase();
  if (!value) return true;
  return !['已签约', '下架', '不可用', 'CLOSED', 'SIGNED', 'OFFLINE'].some((item) => value.includes(item.toUpperCase()));
};

Page({
  data: {
    pageState: 'loading',
    isLoading: true,
    isSubmittingApplication: false,
    showApplyForm: false,
    errorMessage: '',
    loadingText: '加载中...',
    errorTitle: '资源详情加载失败',
    emptyTitle: '未找到资源',
    emptyDescription: '该资源可能已下架或暂未入库。',
    detail: {},
    statusType: 'success',
    canApply: false,
    cannotApplyReason: '',
    form: { title: '', description: '', contactPhone: '' }
  },
  onLoad(query) { this.loadDetail(query.id || '101'); },
  onRetry() { this.loadDetail(this.data.detail.id || '101'); },
  async loadDetail(id) {
    this.setData({ pageState: 'loading', isLoading: true, errorMessage: '' });
    try {
      const detail = await getResourceDetail(id);
      const canApply = canSubmitApplication(detail.investmentStatus);
      this.setData({
        pageState: detail && detail.id ? 'ready' : 'empty',
        isLoading: false,
        detail,
        statusType: getInvestmentStatusType(detail.investmentStatus),
        canApply,
        cannotApplyReason: canApply ? '' : '该资源当前状态不可提交合作申请',
        form: { ...this.data.form, title: `${detail.name || '资源'}合作申请` }
      });
    } catch (error) {
      this.setData({ pageState: 'error', isLoading: false, errorMessage: error.message || '请求失败' });
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
    if (!this.data.isSubmittingApplication) this.setData({ showApplyForm: false });
  },
  onFormInput(event) {
    const key = event.currentTarget.dataset.key;
    if (key) this.setData({ [`form.${key}`]: event.detail.value });
  },
  async onSubmitApplication() {
    const detail = this.data.detail || {};
    const form = this.data.form;
    if (!detail.id || this.data.isSubmittingApplication) return;
    if (!form.title.trim()) return wx.showToast({ title: '请填写申请标题', icon: 'none' });
    if (form.description.trim().length < 10) return wx.showToast({ title: '合作说明至少 10 个字', icon: 'none' });
    if (!PHONE_REG.test(form.contactPhone.trim())) return wx.showToast({ title: '请输入正确手机号', icon: 'none' });
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
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ isSubmittingApplication: false });
    }
  },
  onContact() { wx.showToast({ title: '可在提交申请后等待工作人员对接', icon: 'none' }); }
});
