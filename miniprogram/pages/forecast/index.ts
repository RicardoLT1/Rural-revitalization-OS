import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getForecastView } from '../../services/report';
import type { PageLoadState } from '../../types/common';
import { buildForecastOption } from '../../utils/chart';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u8d8b\u52bf\u9884\u6d4b\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u6682\u65e0\u9884\u6d4b\u6570\u636e',
    emptyDescription: '\u5f53\u524d\u6682\u65e0\u53ef\u7528\u7684\u5ba2\u6d41\u9884\u6d4b\u548c\u7b56\u7565\u5efa\u8bae\u3002',
    forecastOption: buildForecastOption([]),
    band: {},
    risks: [],
    strategies: []
  },
  onLoad() {
    this.loadForecast();
  },
  onRetry() {
    this.loadForecast();
  },
  async loadForecast() {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const view = await getForecastView();
      const isEmpty = !view.forecastData.length && !view.risks.length && !view.strategies.length;
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        forecastOption: buildForecastOption(view.forecastData),
        band: view.band,
        risks: view.risks,
        strategies: view.strategies
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  }
});
