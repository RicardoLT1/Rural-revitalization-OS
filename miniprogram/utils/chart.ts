import { ChartSeries, ForecastPoint, TrendPoint } from '../types';

const baseGrid = { left: 28, right: 16, top: 26, bottom: 30, containLabel: true };

export const buildLineOption = (points: TrendPoint[] | { date: string; value: number }[]) => ({
  kind: 'line',
  unit: '\u4eba\u6b21',
  color: ['#2F7D32'],
  tooltip: { trigger: 'axis' },
  grid: baseGrid,
  xAxis: {
    type: 'category',
    data: points.map((point) => point.date),
    axisLine: { lineStyle: { color: '#D7D1C5' } },
    axisLabel: { color: '#5B6164', fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#ECE7DB' } },
    axisLabel: { color: '#8A9094', fontSize: 11 }
  },
  series: [
    {
      name: '\u5ba2\u6d41',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      lineStyle: { width: 3, color: '#2F7D32' },
      areaStyle: { color: 'rgba(47,125,50,0.18)' },
      data: points.map((point) => point.value)
    }
  ]
});

export const buildBarOption = (labels: string[], series: ChartSeries[]) => ({
  kind: 'bar',
  color: series.map((item) => item.color || '#6FAF5E'),
  tooltip: { trigger: 'axis' },
  grid: baseGrid,
  xAxis: {
    type: 'category',
    data: labels,
    axisLine: { lineStyle: { color: '#D7D1C5' } },
    axisLabel: { color: '#5B6164', fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#ECE7DB' } },
    axisLabel: { color: '#8A9094', fontSize: 11 }
  },
  series: series.map((item) => ({
    name: item.name,
    type: 'bar',
    barMaxWidth: 24,
    itemStyle: { borderRadius: [8, 8, 0, 0] },
    data: item.values
  }))
});

export const buildRingOption = (labels: string[], values: number[], colors: string[]) => ({
  kind: 'ring',
  centerText: '\u6536\u5165\u7ed3\u6784',
  color: colors,
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, icon: 'circle', textStyle: { color: '#5B6164', fontSize: 11 } },
  series: [
    {
      type: 'pie',
      radius: ['54%', '74%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: false,
      label: { show: true, formatter: '{b}: {d}%', color: '#2F3437', fontSize: 11 },
      data: labels.map((label, index) => ({
        name: label,
        value: values[index]
      }))
    }
  ]
});

export const buildForecastOption = (points: ForecastPoint[]) => ({
  kind: 'forecast',
  color: ['#2F7D32', '#D58A2A', '#8FB388'],
  tooltip: { trigger: 'axis' },
  grid: baseGrid,
  xAxis: {
    type: 'category',
    data: points.map((item) => item.date),
    axisLine: { lineStyle: { color: '#D7D1C5' } },
    axisLabel: { color: '#5B6164', fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#ECE7DB' } },
    axisLabel: { color: '#8A9094', fontSize: 11 }
  },
  series: [
    {
      name: '\u9884\u6d4b',
      type: 'line',
      smooth: true,
      lineStyle: { width: 3, color: '#2F7D32' },
      data: points.map((item) => item.predict)
    },
    {
      name: '\u4e0a\u754c',
      type: 'line',
      smooth: true,
      lineStyle: { width: 1, color: '#D58A2A', type: 'dashed' },
      data: points.map((item) => item.upper)
    },
    {
      name: '\u4e0b\u754c',
      type: 'line',
      smooth: true,
      lineStyle: { width: 1, color: '#8FB388', type: 'dashed' },
      data: points.map((item) => item.lower)
    }
  ]
});