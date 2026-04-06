Component({
  properties: {
    risk: { type: Object, value: {} }
  },
  data: {
    levelText: '低风险',
    badgeType: 'info'
  },
  observers: {
    'risk.level': function (level: string) {
      const map: Record<string, { text: string; type: string }> = {
        high: { text: '高风险', type: 'danger' },
        medium: { text: '中风险', type: 'warning' },
        low: { text: '低风险', type: 'info' }
      };
      const found = map[level] || map.low;
      this.setData({ levelText: found.text, badgeType: found.type });
    }
  },
  methods: {
    onTap() {
      const risk = this.properties.risk as { id?: string };
      this.triggerEvent('tap', { id: risk.id });
    }
  }
});
