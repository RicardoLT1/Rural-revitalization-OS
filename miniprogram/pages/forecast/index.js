const { getForecastView } = require('../../services/report');
const { buildForecastOption } = require('../../utils/chart');

Page({
  data: {
    forecastOption: buildForecastOption([]),
    band: {},
    risks: [],
    strategies: []
  },
  onLoad() {
    this.loadForecast();
  },
  async loadForecast() {
    const view = await getForecastView();
    this.setData({
      forecastOption: buildForecastOption(view.forecastData),
      band: view.band,
      risks: view.risks,
      strategies: view.strategies
    });
  }
});
