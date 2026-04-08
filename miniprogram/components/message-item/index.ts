Component({
  properties: {
    messageId: { type: String, value: '' },
    title: { type: String, value: '' },
    time: { type: String, value: '' },
    type: { type: String, value: 'info' }
  },
  data: {
    levelText: '\u901a\u77e5'
  },
  observers: {
    type: function (type: string) {
      const textMap: Record<string, string> = {
        warning: '\u9884\u8b66',
        danger: '\u7d27\u6025',
        info: '\u901a\u77e5',
        success: '\u8fdb\u5c55'
      };
      this.setData({ levelText: textMap[type] || '\u901a\u77e5' });
    }
  },
  methods: {
    onTap() {
      this.triggerEvent('tap', { id: this.properties.messageId });
    }
  }
});