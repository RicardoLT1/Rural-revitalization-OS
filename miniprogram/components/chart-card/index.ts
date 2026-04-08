type PeriodItem = { key: string; label: string };
type PlainObject = Record<string, any>;
type RingItem = { name: string; value: number; color: string; isMajor: boolean };
type CartesianPoint = { x: number; y: number };

Component({
  properties: {
    title: { type: String, value: '' },
    periods: { type: Array, value: [] as PeriodItem[] },
    activePeriod: { type: String, value: '' },
    ecOption: { type: Object, value: {} as PlainObject }
  },

  data: {
    kind: 'line',
    lineCanvasId: 'line-canvas',
    canvasWidth: 320,
    canvasHeight: 150,
    lineLabels: [] as string[],
    lineValues: [] as number[],
    forecastUpper: [] as number[],
    forecastLower: [] as number[],
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
      const info = wx.getSystemInfoSync();
      const unit = info.windowWidth / 750;
      this.setData({
        lineCanvasId: 'line-' + Date.now() + '-' + Math.floor(Math.random() * 1000),
        canvasWidth: Math.max(280, Math.floor(650 * unit)),
        canvasHeight: Math.max(150, Math.floor(280 * unit))
      });
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
          forecastUpper: [],
          forecastLower: [],
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
          (acc: { name: string; value: number; color: string }, cur: { name: string; value: number; color: string }) => {
            return cur.value > acc.value ? cur : acc;
          },
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
          forecastUpper: [],
          forecastLower: [],
          ringLegend,
          ringGradient: 'conic-gradient(' + (gradientStops.join(', ') || '#2F7D32 0% 100%') + ')',
          majorGradient:
            'conic-gradient(transparent 0% ' +
            majorStart.toFixed(2) +
            '%, ' +
            majorRaw.color +
            ' ' +
            majorStart.toFixed(2) +
            '% ' +
            majorEnd.toFixed(2) +
            '%, transparent ' +
            majorEnd.toFixed(2) +
            '% 100%)',
          ringCenterValue: String(majorPercent) + '%',
          ringCenterText: majorRaw.name,
          ringCenterSub: '\u5360\u6bd4\u6700\u9ad8'
        });
        return;
      }

      const labels: string[] = option.xAxis?.data || [];
      const values = this.extractSeriesValues(option, 0);
      const forecastUpper = kind === 'forecast' ? this.extractSeriesValues(option, 1) : [];
      const forecastLower = kind === 'forecast' ? this.extractSeriesValues(option, 2) : [];

      const count = Math.max(labels.length, values.length);
      const safeLabels = count ? labels.slice(0, count) : [];
      while (safeLabels.length < count) {
        safeLabels.push('D' + (safeLabels.length + 1));
      }

      this.setData({
        kind,
        barData: [],
        ringLegend: [],
        lineLabels: safeLabels,
        lineValues: values.slice(0, count),
        forecastUpper: forecastUpper.slice(0, count),
        forecastLower: forecastLower.slice(0, count)
      });

      setTimeout(() => {
        this.drawLineChart();
      }, 0);
    },

    drawPolyline(
      ctx: WechatMiniprogram.CanvasContext,
      points: CartesianPoint[],
      color: string,
      width: number,
      dashed = false
    ) {
      if (!points.length) {
        return;
      }
      ctx.beginPath();
      ctx.moveTo(points[0].x, points[0].y);
      for (let index = 1; index < points.length; index += 1) {
        ctx.lineTo(points[index].x, points[index].y);
      }
      if ((ctx as any).setLineDash) {
        (ctx as any).setLineDash(dashed ? [8, 8] : [], 0);
      }
      ctx.setStrokeStyle(color);
      ctx.setLineWidth(width);
      ctx.setLineCap('round');
      ctx.setLineJoin('round');
      ctx.stroke();
      if ((ctx as any).setLineDash) {
        (ctx as any).setLineDash([], 0);
      }
    },

    drawLineChart() {
      if (!(this.data.kind === 'line' || this.data.kind === 'forecast')) {
        return;
      }

      const values: number[] = this.data.lineValues || [];
      const upper: number[] = this.data.forecastUpper || [];
      const lower: number[] = this.data.forecastLower || [];
      const width = this.data.canvasWidth || 320;
      const height = this.data.canvasHeight || 150;
      const ctx = wx.createCanvasContext(this.data.lineCanvasId, this);
      ctx.clearRect(0, 0, width, height);

      if (!values.length) {
        ctx.draw();
        return;
      }

      const left = 18;
      const right = 10;
      const top = 14;
      const bottom = 24;
      const plotWidth = width - left - right;
      const plotHeight = height - top - bottom;
      const plotBottom = top + plotHeight;

      const pool = values.concat(upper, lower).filter((item) => Number.isFinite(item));
      const max = Math.max(...pool, 1);
      const min = Math.min(...pool, 0);
      const span = max - min || 1;

      for (let i = 0; i < 4; i += 1) {
        const y = top + (plotHeight * i) / 3;
        ctx.beginPath();
        ctx.setStrokeStyle('#ECE7DB');
        ctx.setLineWidth(1);
        ctx.moveTo(left, y);
        ctx.lineTo(width - right, y);
        ctx.stroke();
      }

      const buildPoints = (seriesValues: number[]) => {
        return seriesValues.map((value, index) => {
          const x = left + (plotWidth * index) / Math.max(seriesValues.length - 1, 1);
          const y = plotBottom - ((value - min) / span) * plotHeight;
          return { x, y };
        });
      };

      const points = buildPoints(values);
      const upperPoints = upper.length ? buildPoints(upper) : [];
      const lowerPoints = lower.length ? buildPoints(lower) : [];

      if (upperPoints.length && lowerPoints.length && upperPoints.length === lowerPoints.length) {
        const band = ctx.createLinearGradient(0, top, 0, plotBottom);
        band.addColorStop(0, 'rgba(213,138,42,0.16)');
        band.addColorStop(1, 'rgba(143,179,136,0.06)');
        ctx.beginPath();
        ctx.moveTo(upperPoints[0].x, upperPoints[0].y);
        upperPoints.forEach((point) => ctx.lineTo(point.x, point.y));
        for (let index = lowerPoints.length - 1; index >= 0; index -= 1) {
          const point = lowerPoints[index];
          ctx.lineTo(point.x, point.y);
        }
        ctx.closePath();
        ctx.setFillStyle(band);
        ctx.fill();
      }

      const area = ctx.createLinearGradient(0, top, 0, plotBottom);
      area.addColorStop(0, 'rgba(47,125,50,0.24)');
      area.addColorStop(1, 'rgba(47,125,50,0.03)');
      ctx.beginPath();
      ctx.moveTo(points[0].x, plotBottom);
      points.forEach((point) => ctx.lineTo(point.x, point.y));
      ctx.lineTo(points[points.length - 1].x, plotBottom);
      ctx.closePath();
      ctx.setFillStyle(area);
      ctx.fill();

      if (upperPoints.length && lowerPoints.length) {
        this.drawPolyline(ctx, upperPoints, 'rgba(213,138,42,0.9)', 2, true);
        this.drawPolyline(ctx, lowerPoints, 'rgba(143,179,136,0.9)', 2, true);
      }

      this.drawPolyline(ctx, points, '#2F7D32', 4, false);

      points.forEach((point) => {
        ctx.beginPath();
        ctx.arc(point.x, point.y, 4.2, 0, Math.PI * 2);
        ctx.setFillStyle('#FFFFFF');
        ctx.fill();
        ctx.setStrokeStyle('#2F7D32');
        ctx.setLineWidth(2.4);
        ctx.stroke();
      });

      ctx.draw();
    }
  }
});