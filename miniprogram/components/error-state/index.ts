Component({
  properties: {
    title: { type: String, value: '\u52a0\u8f7d\u5931\u8d25' },
    message: { type: String, value: '\u8bf7\u7a0d\u540e\u91cd\u8bd5' },
    retryText: { type: String, value: '\u91cd\u65b0\u52a0\u8f7d' }
  },
  methods: {
    onRetry() {
      this.triggerEvent('retry');
    }
  }
});