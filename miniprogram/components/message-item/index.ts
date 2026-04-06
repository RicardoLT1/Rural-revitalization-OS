Component({
  properties: {
    messageId: { type: String, value: '' },
    title: { type: String, value: '' },
    time: { type: String, value: '' },
    type: { type: String, value: 'info' }
  },
  data: {
    levelText: '通知'
  },
  observers: {
    'type': function (type: string) {
      const textMap: Record<string, string> = {
        warning: '预警',
        danger: '紧急',
        info: '通知',
        success: '进展'
      };
      this.setData({ levelText: textMap[type] || '通知' });
    }
  },
  methods: {
    onTap() {
      this.triggerEvent('tap', { id: this.properties.messageId });
    }
  }
});
