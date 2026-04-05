const toPath = (url: string): string => `/pages/${url}/index`;

export const goResourceDetail = (id: string) => {
  wx.navigateTo({ url: `${toPath('resource-detail')}?id=${id}` });
};

export const goProcessDetail = (id: string) => {
  wx.navigateTo({ url: `${toPath('process-detail')}?id=${id}` });
};

export const goInvestmentMatch = (id?: string) => {
  const query = id ? `?id=${id}` : '';
  wx.navigateTo({ url: `${toPath('investment-match')}${query}` });
};

export const goForecast = () => {
  wx.navigateTo({ url: toPath('forecast') });
};

export const goReport = () => {
  wx.switchTab({ url: toPath('report') });
};

export const goMap = () => {
  wx.switchTab({ url: toPath('resource-map') });
};

export const goCollab = () => {
  wx.switchTab({ url: toPath('collab') });
};
