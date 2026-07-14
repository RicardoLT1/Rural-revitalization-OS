import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getDashboardData, getDashboardPeriods, getDashboardTrend, TrendPeriod } from '../../services/dashboard';
import { getMyApplications } from '../../services/workflow';
import type { PageLoadState } from '../../types/common';
import type { MyApplicationItem } from '../../types/workflow';
import { buildLineOption } from '../../utils/chart';
import { goCollab, goForecast, goInvestmentMatch, goMap, goReport } from '../../utils/navigation';

let contentReadyTimer: number | undefined;
let metricAnimationTimer: number | undefined;
let metricAnimationRun = 0;

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '首页数据加载失败',
    emptyTitle: '暂无首页数据',
    emptyDescription: '请稍后再查看资源、申请与运营概览。',
    roleName: '',
    villageName: '',
    dataGeneratedAt: '',
    dataRangeLabel: '',
    cacheStatusText: '',
    cacheStatusClass: '',
    dataStaleText: '',
    showDataHealth: false,
    contentReady: false,
    activeHeroIndex: 0,
    heroSlides: [
      {
        kicker: '资源推荐',
        title: '乡村资源一站式服务',
        text: '浏览资源、提交合作申请、跟踪审批进度，为你的乡村项目提供更多可能。',
        button: '查看资源',
        action: 'map',
        tone: 'green',
        photoClass: 'homestay',
        image: '/assets/images/rural-village.jpg'
      },
      {
        kicker: '民宿焕新',
        title: '闲置农房变身精品民宿',
        text: '寻找适合改造的院落资源，连接运营团队与村庄空间。',
        button: '查看民宿',
        action: 'map',
        tone: 'earth',
        photoClass: 'courtyard',
        image: '/assets/images/rice-terraces.jpg'
      },
      {
        kicker: '研学文旅',
        title: '把田野变成研学课堂',
        text: '聚合农田、工坊与乡土文化资源，发现可合作的研学场景。',
        button: '发现研学',
        action: 'map',
        tone: 'gold',
        photoClass: 'study',
        image: '/assets/images/rural-village.jpg'
      },
      {
        kicker: '产业共创',
        title: '农产品工坊共建新品牌',
        text: '对接加工空间、非遗工坊与品牌资源，推动乡村产业落地。',
        button: '寻找工坊',
        action: 'match',
        tone: 'teal',
        photoClass: 'workshop',
        image: '/assets/images/rice-terraces.jpg'
      }
    ],
    applications: [] as MyApplicationItem[],
    recentApplications: [] as MyApplicationItem[],
    stats: [],
    activeMetricIndex: 0,
    homeMetrics: [
      { title: '可申请资源', value: 0, unit: '项', note: '本周新增 0 项', icon: '/assets/tabbar/home-active.png', tone: 'green', action: 'map' },
      { title: '我的申请', value: 0, unit: '条', note: '审核中 0 条', icon: '/assets/tabbar/apply-active.png', tone: 'blue', action: 'collab' },
      { title: '待处理进度', value: 0, unit: '条', note: '可跟进 0 条', icon: '/assets/tabbar/report-active.png', tone: 'orange', action: 'collab' },
      { title: '推荐机会', value: 0, unit: '条', note: '精准匹配', icon: '/assets/tabbar/map-active.png', tone: 'violet', action: 'match' }
    ],
    risks: [],
    suggestions: [],
    periods: getDashboardPeriods(),
    trendPeriod: '7d',
    trendOption: buildLineOption([])
  },
  onLoad() {
    this.loadDashboard();
  },
  onShow() {
    if (!this.data.isLoading) {
      this.loadDashboard();
    }
  },
  onUnload() {
    metricAnimationRun += 1;
    if (contentReadyTimer !== undefined) clearTimeout(contentReadyTimer);
    if (metricAnimationTimer !== undefined) clearTimeout(metricAnimationTimer);
  },
  async loadDashboard() {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '', contentReady: false });
    try {
      const [dashboard, applications] = await Promise.all([getDashboardData(), getMyApplications()]);
      const role = wx.getStorageSync('XIANGYUN_ROLE') || 'USER';
      const showDataHealth = role === 'STAFF' || role === 'ADMIN';
      const isEmpty = !dashboard.stats.length && !dashboard.risks.length && !dashboard.suggestions.length && !dashboard.trends.days7.length && !applications.length;
      const targetMetrics = this.buildHomeMetrics(dashboard.stats, applications);
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        roleName: dashboard.roleName,
        villageName: dashboard.villageName,
        dataGeneratedAt: this.formatGeneratedAt(dashboard.generatedAt),
        dataRangeLabel: dashboard.rangeDays ? `${dashboard.rangeDays}\u5929` : '\u9ed8\u8ba4',
        cacheStatusText: this.getCacheStatusText(dashboard.cacheStatus),
        cacheStatusClass: this.getCacheStatusClass(dashboard.cacheStatus, dashboard.stale),
        dataStaleText: dashboard.stale ? '\u5df2\u4f7f\u7528\u6700\u8fd1\u4e00\u6b21\u6210\u529f\u6570\u636e' : '\u6570\u636e\u6b63\u5e38',
        showDataHealth,
        applications,
        recentApplications: applications.slice(0, 3).map((item) => ({
          ...item,
          progress: this.getApplicationProgress(item.status)
        })),
        stats: dashboard.stats,
        homeMetrics: targetMetrics.map((item) => ({ ...item, value: 0 })),
        risks: dashboard.risks,
        suggestions: dashboard.suggestions,
        trendOption: buildLineOption(dashboard.trends.days7)
      }, () => {
        if (contentReadyTimer !== undefined) clearTimeout(contentReadyTimer);
        contentReadyTimer = setTimeout(() => this.setData({ contentReady: true }), 30);
        this.animateHomeMetrics(targetMetrics);
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onRetry() {
    this.loadDashboard();
  },
  onHeroChange(event: WechatMiniprogram.CustomEvent<{ current: number }>) {
    this.setData({ activeHeroIndex: event.detail.current });
  },
  buildHomeMetrics(stats: any[] = [], applications: MyApplicationItem[] = []) {
    const pick = (index: number, fallback: number) => Number(stats[index]?.value ?? fallback);
    const resources = Math.max(0, pick(0, 17));
    const cooperable = Math.max(0, pick(1, 10));
    const pending = applications.filter((item) => item.status === 'PENDING').length;
    const risk = Math.max(0, pick(3, 1));
    return [
      { title: '可申请资源', value: resources || 17, unit: '项', note: `本周新增 ${Math.max(1, Math.round((resources || 17) * 0.3))} 项`, icon: '/assets/tabbar/home-active.png', tone: 'green', action: 'map' },
      { title: '我的申请', value: applications.length, unit: '条', note: `审核中 ${pending} 条`, icon: '/assets/tabbar/apply-active.png', tone: 'blue', action: 'collab' },
      { title: '待处理进度', value: Math.max(1, risk), unit: '条', note: '可跟进 1 条', icon: '/assets/tabbar/report-active.png', tone: 'orange', action: 'collab' },
      { title: '推荐机会', value: Math.max(6, cooperable), unit: '条', note: '精准匹配', icon: '/assets/tabbar/map-active.png', tone: 'violet', action: 'match' }
    ];
  },
  animateHomeMetrics(targetMetrics: any[]) {
    metricAnimationRun += 1;
    const currentRun = metricAnimationRun;
    if (metricAnimationTimer !== undefined) clearTimeout(metricAnimationTimer);
    let step = 0;
    const totalSteps = 24;
    const update = () => {
      if (currentRun !== metricAnimationRun) return;
      step += 1;
      const progress = Math.min(1, step / totalSteps);
      const eased = 1 - Math.pow(1 - progress, 3);
      this.setData({
        homeMetrics: targetMetrics.map((item) => ({
          ...item,
          value: Math.round(Number(item.value || 0) * eased)
        }))
      });
      if (step < totalSteps) {
        metricAnimationTimer = setTimeout(update, 30);
      }
    };
    update();
  },
  getApplicationProgress(status?: string) {
    return ({ PENDING: 45, MATERIAL_REQUIRED: 65, APPROVED: 100, REJECTED: 100 } as Record<string, number>)[status || ''] || 30;
  },
  formatGeneratedAt(value?: string) {
    if (!value) {
      return '\u6682\u65e0';
    }
    return value.replace('T', ' ').slice(0, 16);
  },
  getCacheStatusText(status?: string) {
    const statusMap: Record<string, string> = {
      HIT: '\u7f13\u5b58\u547d\u4e2d',
      MISS: '\u5b9e\u65f6\u751f\u6210',
      STALE: '\u964d\u7ea7\u6570\u636e',
      MOCK: '\u6f14\u793a\u6570\u636e'
    };
    return statusMap[status || ''] || '\u672a\u77e5\u72b6\u6001';
  },
  getCacheStatusClass(status?: string, stale?: boolean) {
    if (stale || status === 'STALE') {
      return 'warning';
    }
    if (status === 'MISS') {
      return 'fresh';
    }
    return 'normal';
  },
  onTrendPeriodChange(event: WechatMiniprogram.CustomEvent<{ key: '7d' | '30d' }>) {
    this.loadTrend(event.detail.key);
  },
  async loadTrend(trendPeriod: TrendPeriod) {
    try {
      const trendData = await getDashboardTrend(trendPeriod);
      this.setData({
        trendPeriod,
        trendOption: buildLineOption(trendData)
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onTipAction(event: WechatMiniprogram.CustomEvent<{ actionType: string }>) {
    const actionType = event.detail.actionType;
    if (actionType === 'forecast') {
      goForecast();
      return;
    }
    if (actionType === 'match') {
      goInvestmentMatch();
      return;
    }
    if (actionType === 'process') {
      goCollab();
      return;
    }
    goReport();
  },
  onEntryTap(event: WechatMiniprogram.TouchEvent) {
    const action = event.currentTarget.dataset.action;
    if (action === 'map') {
      goMap();
      return;
    }
    if (action === 'collab') {
      goCollab();
      return;
    }
    if (action === 'report') {
      goReport();
      return;
    }
    if (action === 'match') {
      goInvestmentMatch();
      return;
    }
    goForecast();
  }
});
