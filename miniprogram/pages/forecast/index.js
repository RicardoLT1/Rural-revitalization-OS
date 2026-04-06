const { forecastBand, forecastData, forecastRisks, forecastStrategies } = require('../../mock/forecast');
const { buildForecastOption } = require('../../utils/chart');

Page({
  data: {
    forecastOption: buildForecastOption(forecastData),
    band: forecastBand,
    risks: forecastRisks,
    strategies: forecastStrategies
  }
});