Component({
  properties: {
    node: { type: Object, value: {} },
    expanded: { type: Boolean, value: false }
  },
  data: {
    statusText: '待处理',
    badgeType: 'neutral'
  },
  observers: {
    'node.status': function (status: string) {
      const map: Record<string, { text: string; type: string }> = {
        done: { text: '已完成', type: 'success' },
        doing: { text: '进行中', type: 'info' },
        pending: { text: '待处理', type: 'warning' },
        blocked: { text: '阻塞', type: 'danger' }
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
