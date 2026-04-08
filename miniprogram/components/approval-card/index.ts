Component({
  properties: {
    approval: { type: Object, value: {} }
  },
  data: {
    statusType: 'neutral'
  },
  observers: {
    'approval.status': function (status: string) {
      const map: Record<string, string> = {
        '\u5f85\u5ba1\u6279': 'warning',
        '\u5df2\u9a73\u56de': 'danger',
        '\u5df2\u901a\u8fc7': 'success'
      };
      this.setData({ statusType: map[status] || 'neutral' });
    }
  },
  methods: {
    onTap() {
      const approval = this.properties.approval as { processId?: string };
      this.triggerEvent('tap', { processId: approval.processId });
    },
    onApprove(event: WechatMiniprogram.TouchEvent) {
      const action = event.currentTarget.dataset.action;
      const approval = this.properties.approval as { id?: string; processId?: string };
      this.triggerEvent('approve', { action, id: approval.id, processId: approval.processId });
    }
  }
});