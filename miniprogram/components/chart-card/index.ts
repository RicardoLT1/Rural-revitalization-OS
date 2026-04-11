type PeriodItem = { key: string; label: string };
type PlainObject = Record<string, any>;
type RingItem = { name: string; value: number; color: string; isMajor: boolean };
type LinePoint = { left: number; top: number; valueLabel: string };
type LineSegment = { left: number; top: number; width: number; rotate: number };

Component({
  properties: {
    title: { type: String, value: '' },
    periods: { type: Array, value: [] as PeriodItem[] },
    activePeriod: { type: String, value: '' },
    ecOption: { type: Object, value: {} as PlainObject }
  },

  data: {
    kind: 'line',
    lineLabels: [] as string[],
    lineValues: [] as number[],
    linePoints: [] as LinePoint[],
    lineSegments: [] as LineSegment[],
    barData: [] as Array<{ label: string; value: number; height: number }>,
    ringLegend: [] as RingItem[],
    ringGradient: 'conic-gradient(#2F7D32 0% 100%)',
    majorGradient: 'conic-gradient(transparent 0% 100%)',
    ringCenterValue: '100%',
    ringCenterText: '\u6536\u5165\u7ed3\u6784',
    ringCenterSub: '\u6587\u65c5\u6536\u5165\u5360\u6bd4\u6700\u9ad8'
  },

  lifetimes: {
    attached() {
      this.applyOption(this.properties.ecOption as PlainObject);
    }
  },

  observers: {
    ecOption: function (option: PlainObject) {
      this.applyOption(option || {});
    }
  },

  methods: {
    onPeriodTap(event: WechatMiniprogram.TouchEvent) {
      const key = event.currentTarget.dataset.key;
      this.triggerEvent('periodchange', { key });
    },

    extractSeriesValues(option: PlainObject, index: number): number[] {
      const series = option.series?.[index] || {};
      const raw = Array.isArray(series.data)
        ? series.data
        : Array.isArray(series.values)
          ? series.values
          : [];
      return raw.map((item: number | { value?: number }) => {
        if (typeof item === 'number') {
          return Number.isFinite(item) ? item : 0;
        }
        return Number(item?.value) || 0;
      });
    },

    buildLinePoints(values: number[]): LinePoint[] {
      if (!values.length) {
        return [];
      }
      const max = Math.max(...values, 1);
      const min = Math.min(...values, 0);
      const span = max - min || 1;
      return values.map((value, index) => ({
        left: Number(((index / Math.max(values.length - 1, 1)) * 100).toFixed(2)),
        top: Number((100 - ((value - min) / span) * 82 - 8).toFixed(2)),
        valueLabel: value >= 10000 ? (Number((value / 10000).toFixed(1)) + '\u4e07') : String(value)
      }));
    },

    buildLineSegments(points: LinePoint[]): LineSegment[] {
      return points.slice(0, -1).map((point, index) => {
        const next = points[index + 1];
        const dx = next.left - point.left;
        const dy = next.top - point.top;
        return {
          left: point.left,
          top: point.top,
          width: Number(Math.sqrt(dx * dx + dy * dy).toFixed(2)),
          rotate: Number((Math.atan2(dy, dx) * 180 / Math.PI).toFixed(2))
        };
      });
    },

    applyOption(option: PlainObject) {
      const kind = option.kind || 'line';

      if (kind === 'bar') {
        const labels: string[] = option.xAxis?.data || [];
        const values = this.extractSeriesValues(option, 0);
        const count = Math.max(labels.length, values.length);
        const max = Math.max(...values, 1);
        const barData = Array.from({ length: count }).map((_, index) => {
          const value = Number(values[index] || 0);
          return {
            label: labels[index] || ('\u7b2c' + (index + 1) + '\u5929'),
            value,
            height: Math.max(Number(((value / max) * 100).toFixed(2)), 12)
          };
        });

        this.setData({
          kind,
          lineLabels: [],
          lineValues: [],
          linePoints: [],
          lineSegments: [],
          ringLegend: [],
          barData
        });
        return;
      }

      if (kind === 'ring') {
        const colors: string[] = option.color || [];
        const source = (option.series?.[0]?.data || []).map((item: PlainObject, index: number) => ({
          name: item.name || ('\u7c7b\u578b' + (index + 1)),
          value: Number(item.value) || 0,
          color: colors[index] || '#2F7D32'
        }));
        const total = source.reduce((sum: number, item: PlainObject) => sum + item.value, 0) || 1;
        const majorRaw = source.reduce(
          (acc: { name: string; value: number; color: string }, cur: { name: string; value: number; color: string }) => cur.value > acc.value ? cur : acc,
          source[0] || { name: '\u6587\u65c5\u6536\u5165', value: 0, color: '#2F7D32' }
        );
        const majorPercent = Math.round((majorRaw.value / total) * 100);
        let progress = 0;
        let majorStart = 0;
        let majorEnd = 0;

        const ringLegend: RingItem[] = source.map((item) => {
          const ratio = (item.value / total) * 100;
          const start = progress;
          const end = progress + ratio;
          progress = end;
          const isMajor = item.name === majorRaw.name;
          if (isMajor) {
            majorStart = start;
            majorEnd = end;
          }
          return {
            name: item.name,
            value: Math.round((item.value / total) * 100),
            color: item.color,
            isMajor
          };
        });

        progress = 0;
        const gradientStops: string[] = [];
        source.forEach((item) => {
          const ratio = (item.value / total) * 100;
          const start = progress;
          const end = progress + ratio;
          const split = Math.max(start, end - 0.55);
          gradientStops.push(item.color + ' ' + start.toFixed(2) + '% ' + split.toFixed(2) + '%');
          gradientStops.push('#FFFFFF ' + split.toFixed(2) + '% ' + end.toFixed(2) + '%');
          progress = end;
        });

        this.setData({
          kind,
          barData: [],
          lineLabels: [],
          lineValues: [],
          linePoints: [],
          lineSegments: [],
          ringLegend,
          ringGradient: 'conic-gradient(' + (gradientStops.join(', ') || '#2F7D32 0% 100%') + ')',
          majorGradient: 'conic-gradient(transparent 0% ' + majorStart.toFixed(2) + '%, ' + majorRaw.color + ' ' + majorStart.toFixed(2) + '% ' + majorEnd.toFixed(2) + '%, transparent ' + majorEnd.toFixed(2) + '% 100%)',
          ringCenterValue: String(majorPercent) + '%',
          ringCenterText: majorRaw.name,
          ringCenterSub: '\u5360\u6bd4\u6700\u9ad8'
        });
        return;
      }

      const labels: string[] = option.xAxis?.data || [];
      const values = this.extractSeriesValues(option, 0);
      const count = Math.max(labels.length, values.length);
      const safeLabels = count ? labels.slice(0, count) : [];
      while (safeLabels.length < count) {
        safeLabels.push('D' + (safeLabels.length + 1));
      }
      const safeValues = values.slice(0, count);
      const linePoints = this.buildLinePoints(safeValues);

      this.setData({
        kind,
        barData: [],
        ringLegend: [],
        lineLabels: safeLabels,
        lineValues: safeValues,
        linePoints,
        lineSegments: this.buildLineSegments(linePoints)
      });
    }
  }
});
