import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getInvestmentStatusType, getResourceDetail } from '../../services/resource';
import type { PageLoadState } from '../../types/common';
import type { ResourceDetail } from '../../types/resource';
import { goInvestmentMatch } from '../../utils/navigation';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u8d44\u6e90\u8be6\u60c5\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u672a\u627e\u5230\u8d44\u6e90',
    emptyDescription: '\u8be5\u8d44\u6e90\u53ef\u80fd\u5df2\u4e0b\u67b6\u6216\u6682\u672a\u5165\u5e93\u3002',
    detail: {} as Partial<ResourceDetail>,
    statusType: 'success'
  },
  onLoad(query: Record<string, string>) {
    const id = query.id || 'res-01';
    this.loadDetail(id);
  },
  onRetry() {
    this.loadDetail((this.data.detail as Partial<ResourceDetail>).id || 'res-01');
  },
  async loadDetail(id: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const detail = await getResourceDetail(id);
      this.setData({
        pageState: detail?.id ? PageState.Ready : PageState.Empty,
        isLoading: false,
        detail,
        statusType: getInvestmentStatusType(detail.investmentStatus)
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onGoMatch() {
    const id = (this.data.detail as Partial<ResourceDetail>).id;
    if (id) {
      goInvestmentMatch(id);
    }
  },
  onMockAction(event: WechatMiniprogram.TouchEvent) {
    const name = event.currentTarget.dataset.name;
    wx.showToast({ title: `${name}\uff08\u6f14\u793a\u6001\uff09`, icon: 'none' });
  }
});
