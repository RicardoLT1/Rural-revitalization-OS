Component({
  properties: {
    risk: { type: Object, value: {} }
  },
  data: {
    levelText: '\u4f4e\u98ce\u9669',
    badgeType: 'info'
  },
  observers: {
    'risk.level': function (level: string) {
      const map: Record<string, { text: string; type: string }> = {
        high: { text: '\u9ad8\u98ce\u9669', type: 'danger' },
        medium: { text: '\u4e2d\u98ce\u9669', type: 'warning' },
        low: { text: '\u4f4e\u98ce\u9669', type: 'info' }
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