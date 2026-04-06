type PeriodItem = { key: string; label: string };

type PlainObject = Record<string, any>;

Component({
  properties: {
    title: { type: String, value: '' },
    periods: { type: Array, value: [] as PeriodItem[] },
    activePeriod: { type: String, value: '' },
    ecOption: { type: Object, value: {} as PlainObject }
  },
  data: {
    kind: 'line',
    linePoints: [] as Array<{ left: number; bottom: number }>,
    barData: [] as Array<{ label: string; height: number }>,
    ringLegend: [] as Array<{ name: string; value: number; color: string }>
  },
  observers: {
    'ecOption': function (option: PlainObject) {
      this.applyOption(option || {});
    }
  },
  methods: {
    onPeriodTap(event: WechatMiniprogram.TouchEvent) {
      const key = event.currentTarget.dataset.key;
      this.triggerEvent('periodchange', { key });
    },
    applyOption(option: PlainObject) {
      const kind = option.kind || 'line';
      if (kind === 'bar') {
        const labels: string[] = option.xAxis?.data || [];
        const values: number[] = option.series?.[0]?.data || [];
        const max = Math.max(...values, 1);
        this.setData({
          kind,
          barData: values.map((value, index) => ({
            label: labels[index] || `${index + 1}`,
            height: Number(((value / max) * 100).toFixed(2))
          }))
        });
        return;
      }
      if (kind === 'ring') {
        const colors: string[] = option.color || [];
        const ringLegend = (option.series?.[0]?.data || []).map((item: PlainObject, index: number) => ({
          name: item.name,
          value: item.value,
          color: colors[index] || '#2f7d32'
        }));
        this.setData({ kind, ringLegend });
        return;
      }
      const values: number[] = option.series?.[0]?.data || [];
      const max = Math.max(...values, 1);
      const min = Math.min(...values, 0);
      const span = max - min || 1;
      const linePoints = values.map((value, index) => ({
        left: Number(((index / Math.max(values.length - 1, 1)) * 100).toFixed(2)),
        bottom: Number((((value - min) / span) * 100).toFixed(2))
      }));
      this.setData({ kind, linePoints });
    }
  }
});
