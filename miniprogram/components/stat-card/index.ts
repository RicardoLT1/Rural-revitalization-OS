Component({
  properties: {
    title: { type: String, value: '' },
    value: { type: Number, value: 0 },
    unit: { type: String, value: '' },
    delta: { type: Number, value: 0 },
    trend: { type: String, value: 'flat' },
    status: { type: String, value: 'neutral' },
    icon: { type: String, value: '' }
  },
  data: {
    trendText: '',
    statusText: ''
  },
  observers: {
    'delta, trend, status': function (delta: number, trend: string, status: string) {
      const prefix = delta > 0 ? '+' : '';
      const arrow = trend === 'up' ? '\u25b2' : trend === 'down' ? '\u25bc' : '\u2022';
      const statusMap: Record<string, string> = {
        success: '\u8fd0\u884c\u5e73\u7a33',
        warning: '\u9700\u8981\u5173\u6ce8',
        danger: '\u9ad8\u98ce\u9669',
        info: '\u91cd\u70b9\u6307\u6807',
        neutral: '\u5e38\u89c4'
      };
      this.setData({
        trendText: `${arrow} ${prefix}${delta}%`,
        statusText: statusMap[status] || statusMap.neutral
      });
    }
  },
  methods: {
    onTap() {
      this.triggerEvent('tap');
    }
  }
});