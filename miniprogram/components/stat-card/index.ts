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
      const arrow = trend === 'up' ? '▲' : trend === 'down' ? '▼' : '•';
      const statusMap: Record<string, string> = {
        success: '运行平稳',
        warning: '需关注',
        danger: '高风险',
        info: '重点指标',
        neutral: '常规'
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
