import { forecastBand, forecastData, forecastRisks, forecastStrategies } from '../../mock/forecast';
import { buildForecastOption } from '../../utils/chart';

Page({
  data: {
    forecastOption: buildForecastOption(forecastData),
    band: forecastBand,
    risks: forecastRisks,
    strategies: forecastStrategies
  }
});
