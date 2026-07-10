import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { approveWorkflow, getCollabWorkbench, getMyApplications, rejectWorkflow, requireWorkflowMaterial } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import type { MyApplicationItem, TodoViewItem } from '../../types/workflow';
import { goProcessDetail } from '../../utils/navigation';

const statusOptions = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING', label: '待审批' },
  { key: 'APPROVED', label: '已通过' },
  { key: 'REJECTED', label: '已驳回' }
];

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '我的申请加载失败',
    emptyTitle: '暂无合作申请',
    emptyDescription: '在资源详情页提交合作申请后，可在这里查看审批进度。',
    statusOptions,
    activeStatus: 'ALL',
    role: 'USER',
    isStaffMode: false,
    pendingTodos: [] as TodoViewItem[],
    allApplications: [] as MyApplicationItem[],
    myApplications: [] as MyApplicationItem[]
  },

  onLoad() {
    this.loadApplications();
  },

  onShow() {
    if (!this.data.isLoading) {
      this.loadApplications();
    }
  },

  onPullDownRefresh() {
    this.loadApplications().finally(() => wx.stopPullDownRefresh());
  },

  onRetry() {
    this.loadApplications();
  },

  onStatusTap(event: WechatMiniprogram.TouchEvent) {
    const value = event.currentTarget.dataset.value;
    if (value) {
      this.setData({ activeStatus: value }, () => this.applyFilter());
    }
  },

  async loadApplications() {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const role = wx.getStorageSync('XIANGYUN_ROLE') || 'USER';
      const isStaffMode = role === 'STAFF' || role === 'ADMIN';
      if (isStaffMode) {
        const workbench = await getCollabWorkbench('全部');
        const pendingTodos = (workbench.filteredTodos || []).filter((item) => item.status === 'PENDING' || item.status === 'MATERIAL_REQUIRED');
        this.setData({
          role,
          isStaffMode,
          pendingTodos,
          isLoading: false,
          emptyTitle: '暂无待处理申请',
          emptyDescription: '当前没有需要审批或补充材料的合作申请。',
          pageState: pendingTodos.length ? PageState.Ready : PageState.Empty
        });
        return;
      }

      const list = await getMyApplications();
      this.setData({
        role,
        isStaffMode,
        allApplications: list,
        isLoading: false,
        emptyTitle: '暂无合作申请',
        emptyDescription: '在资源详情页提交合作申请后，可在这里查看审批进度。'
      }, () => this.applyFilter());
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },

  applyFilter() {
    const activeStatus = this.data.activeStatus;
    const myApplications = activeStatus === 'ALL'
      ? this.data.allApplications
      : this.data.allApplications.filter((item) => item.status === activeStatus);
    this.setData({
      myApplications,
      pageState: myApplications.length ? PageState.Ready : PageState.Empty
    });
  },

  onApplicationTap(event: WechatMiniprogram.TouchEvent) {
    const processId = event.currentTarget.dataset.processid;
    if (processId) {
      goProcessDetail(processId);
    }
  },

  onTodoTap(event: WechatMiniprogram.TouchEvent) {
    const processId = event.currentTarget.dataset.processid;
    if (processId) {
      goProcessDetail(processId);
    }
  },

  async onTodoAction(event: WechatMiniprogram.TouchEvent) {
    const processId = event.currentTarget.dataset.processid;
    const action = event.currentTarget.dataset.action;
    if (!processId || !action) {
      return;
    }
    const actionTextMap: Record<string, string> = {
      approve: '通过申请',
      reject: '驳回申请',
      material: '要求补充材料'
    };
    wx.showModal({
      title: actionTextMap[action] || '处理申请',
      content: '确认后会写入审批记录和操作日志。',
      confirmText: '确认',
      success: async (result) => {
        if (!result.confirm) {
          return;
        }
        try {
          if (action === 'approve') {
            await approveWorkflow(processId, '审批通过');
          } else if (action === 'reject') {
            await rejectWorkflow(processId, '审批驳回');
          } else {
            await requireWorkflowMaterial(processId, '请补充联系人、材料说明或现场照片');
          }
          wx.showToast({ title: '已处理', icon: 'success' });
          this.loadApplications();
        } catch (error) {
          wx.showToast({ title: getErrorMessage(error), icon: 'none' });
        }
      }
    });
  }
});
