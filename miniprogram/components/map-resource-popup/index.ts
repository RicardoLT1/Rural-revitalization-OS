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
        可招商: 'success',
        洽谈中: 'warning',
        已签约: 'info'
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
