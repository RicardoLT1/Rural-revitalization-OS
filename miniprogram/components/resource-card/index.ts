Component({
  properties: {
    resource: { type: Object, value: {} },
    showActions: { type: Boolean, value: false }
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
    },
    onAction(event: WechatMiniprogram.TouchEvent) {
      const action = event.currentTarget.dataset.action;
      const current = this.properties.resource as { id?: string };
      this.triggerEvent(action, { id: current.id });
    }
  }
});
