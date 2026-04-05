export const formatNumber = (value: number): string => {
  return value.toLocaleString('zh-CN');
};

export const formatCurrencyWan = (value: number): string => {
  return `${value.toFixed(1)} 万`;
};

export const formatPercent = (value: number): string => {
  return `${value.toFixed(1)}%`;
};

export const trendPrefix = (delta: number): string => {
  if (delta > 0) {
    return `+${delta}`;
  }
  return `${delta}`;
};
