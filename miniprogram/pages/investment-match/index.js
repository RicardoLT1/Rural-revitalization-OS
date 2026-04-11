const { getInvestmentMatchView } = require('../../services/report');
const { goForecast, goResourceDetail } = require('../../utils/navigation');

Page({
  data: {
    resource: {},
    matches: [],
    aiSummary: {}
  },
  onLoad(query) {
    const id = query.id || 'res-01';
    this.loadMatches(id);
  },
  async loadMatches(id) {
    const view = await getInvestmentMatchView(id);
    this.setData(view);
  },
  onResourceTap() {
    goResourceDetail(this.data.resource.id);
  },
  onAiAction() {
    goForecast();
  }
});
