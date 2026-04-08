Component({
  properties: {
    title: { type: String, value: '' },
    periods: { type: Array, value: [] },
    activePeriod: { type: String, value: '' },
    ecOption: { type: Object, value: {} }
  },
  methods: {
    onPeriodChange(event: WechatMiniprogram.CustomEvent<{ key: string }>) {
      this.triggerEvent('periodchange', event.detail);
    }
  }
});