import { ChartSeries, ForecastPoint, TrendPoint } from '../types';

const baseGrid = { left: 28, right: 14, top: 22, bottom: 28, containLabel: true };

export const buildLineOption = (points: TrendPoint[] | { date: string; value: number }[]) => ({
  kind: 'line',
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
      name: '客流',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 3, color: '#2F7D32' },
      areaStyle: { color: 'rgba(47,125,50,0.16)' },
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
  color: colors,
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, icon: 'circle', textStyle: { color: '#5B6164', fontSize: 11 } },
  series: [
    {
      type: 'pie',
      radius: ['52%', '72%'],
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
      name: '预测',
      type: 'line',
      smooth: true,
      lineStyle: { width: 3, color: '#2F7D32' },
      data: points.map((item) => item.predict)
    },
    {
      name: '上界',
      type: 'line',
      smooth: true,
      lineStyle: { width: 1, color: '#D58A2A', type: 'dashed' },
      data: points.map((item) => item.upper)
    },
    {
      name: '下界',
      type: 'line',
      smooth: true,
      lineStyle: { width: 1, color: '#8FB388', type: 'dashed' },
      data: points.map((item) => item.lower)
    }
  ]
});
