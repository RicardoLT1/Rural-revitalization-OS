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
        待审批: 'warning',
        已退回: 'danger',
        已通过: 'success'
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
