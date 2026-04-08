Component({
  properties: {
    nodes: { type: Array, value: [] },
    currentNodeId: { type: String, value: '' }
  },
  methods: {
    onTap(event: WechatMiniprogram.TouchEvent) {
      const id = event.currentTarget.dataset.id;
      this.triggerEvent('nodetap', { id });
    }
  }
});