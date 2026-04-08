Component({
  properties: {
    node: { type: Object, value: {} },
    expanded: { type: Boolean, value: false }
  },
  data: {
    statusText: '\u5f85\u5904\u7406',
    badgeType: 'neutral'
  },
  observers: {
    'node.status': function (status: string) {
      const map: Record<string, { text: string; type: string }> = {
        done: { text: '\u5df2\u5b8c\u6210', type: 'success' },
        doing: { text: '\u8fdb\u884c\u4e2d', type: 'info' },
        pending: { text: '\u5f85\u5904\u7406', type: 'warning' },
        blocked: { text: '\u963b\u585e', type: 'danger' }
      };
      const found = map[status] || map.pending;
      this.setData({ statusText: found.text, badgeType: found.type });
    }
  },
  methods: {
    onToggle() {
      this.triggerEvent('toggle', { id: this.properties.node.id });
    }
  }
});