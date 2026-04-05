Component({
  properties: {
    options: { type: Array, value: [] },
    activeKey: { type: String, value: '' }
  },
  methods: {
    onSelect(event: WechatMiniprogram.TouchEvent) {
      const key = event.currentTarget.dataset.key;
      this.triggerEvent('change', { key });
    }
  }
});
