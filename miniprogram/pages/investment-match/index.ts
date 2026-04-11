import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getInvestmentMatchView } from '../../services/report';
import type { PageLoadState } from '../../types/common';
import type { InvestmentMatchViewItem, ResourceDetail } from '../../types/resource';
import { goForecast, goResourceDetail } from '../../utils/navigation';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u62db\u5546\u63a8\u8350\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u6682\u65e0\u5339\u914d\u7ed3\u679c',
    emptyDescription: '\u5f53\u524d\u8d44\u6e90\u6682\u672a\u5339\u914d\u5230\u5408\u9002\u7684\u62db\u5546\u5bf9\u8c61\u3002',
    resource: {} as Partial<ResourceDetail>,
    matches: [] as InvestmentMatchViewItem[],
    aiSummary: {}
  },
  onLoad(query: Record<string, string>) {
    const id = query.id || 'res-01';
    this.loadMatches(id);
  },
  onRetry() {
    this.loadMatches((this.data.resource as Partial<ResourceDetail>).id || 'res-01');
  },
  async loadMatches(id: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const view = await getInvestmentMatchView(id);
      this.setData({
        ...view,
        pageState: view.matches.length ? PageState.Ready : PageState.Empty,
        isLoading: false
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onResourceTap() {
    const id = (this.data.resource as Partial<ResourceDetail>).id;
    if (id) {
      goResourceDetail(id);
    }
  },
  onAiAction() {
    goForecast();
  }
});
