Component({
  properties: {
    tip: { type: Object, value: {} }
  },
  methods: {
    onTap() {
      const tip = this.properties.tip as { id?: string; actionType?: string };
      this.triggerEvent('action', { id: tip.id, actionType: tip.actionType });
    }
  }
});
