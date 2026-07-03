const { getResourceDetail, getInvestmentStatusType } = require('../../services/resource');
const { submitCooperationApplication } = require('../../services/workflow');
const { goInvestmentMatch } = require('../../utils/navigation');

Page({
  data: {
    pageState: 'loading',
    isLoading: true,
    isSubmittingApplication: false,
    errorMessage: '',
    loadingText: '加载中...',
    errorTitle: '资源详情加载失败',
    emptyTitle: '未找到资源',
    emptyDescription: '该资源可能已下架或暂未入库。',
    detail: {},
    statusType: 'success'
  },
  onLoad(query) {
    const id = query.id || '101';
    this.loadDetail(id);
  },
  onRetry() {
    this.loadDetail(this.data.detail.id || '101');
  },
  async loadDetail(id) {
    this.setData({ pageState: 'loading', isLoading: true, errorMessage: '' });
    try {
      const detail = await getResourceDetail(id);
      this.setData({
        pageState: detail && detail.id ? 'ready' : 'empty',
        isLoading: false,
        detail,
        statusType: getInvestmentStatusType(detail.investmentStatus)
      });
    } catch (error) {
      this.setData({ pageState: 'error', isLoading: false, errorMessage: error.message || '请求失败' });
    }
  },
  onGoMatch() {
    if (this.data.detail.id) {
      goInvestmentMatch(this.data.detail.id);
    }
  },
  async onSubmitApplication() {
    const detail = this.data.detail || {};
    if (!detail.id || this.data.isSubmittingApplication) return;
    this.setData({ isSubmittingApplication: true });
    try {
      await submitCooperationApplication({
        resourceId: detail.id,
        title: `${detail.name || '资源'}合作申请`
      });
      wx.showToast({ title: '申请已提交', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/collab/index' });
      }, 600);
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ isSubmittingApplication: false });
    }
  },
  onContact() {
    wx.showToast({ title: '请等待工作人员审批对接', icon: 'none' });
  }
});