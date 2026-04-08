Component({
  properties: {
    resource: { type: Object, value: {} }
  },
  data: {
    statusType: 'neutral'
  },
  observers: {
    'resource.investmentStatus': function (status: string) {
      const map: Record<string, string> = {
        '\u53ef\u62db\u5546': 'success',
        '\u6d3d\u8c08\u4e2d': 'warning',
        '\u5df2\u7b7e\u7ea6': 'info'
      };
      this.setData({ statusType: map[status] || 'neutral' });
    }
  },
  methods: {
    onTap() {
      const current = this.properties.resource as { id?: string };
      this.triggerEvent('tap', { id: current.id });
    }
  }
});