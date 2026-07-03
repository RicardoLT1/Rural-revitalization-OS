const { getDashboardData, getDashboardPeriods, getDashboardTrend } = require('../../services/dashboard');
const { getMyApplications } = require('../../services/workflow');
const { buildLineOption } = require('../../utils/chart');
const { goCollab, goForecast, goInvestmentMatch, goMap, goReport } = require('../../utils/navigation');

const PageState = { Loading: 'loading', Ready: 'ready', Empty: 'empty', Error: 'error' };
const DEFAULT_LOADING_TEXT = '正在加载乡耘 OS 数据...';
const getErrorMessage = (error) => error && error.message ? error.message : '首页数据加载失败，请稍后重试';

Page({
  data: {
    pageState: PageState.Loading,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '首页数据加载失败',
    emptyTitle: '暂无首页数据',
    emptyDescription: '请稍后再查看资源、申请与运营概览。',
    roleName: '',
    villageName: '',
    activeHeroIndex: 0,
    heroSlides: [
      { kicker: '资源推荐', title: '乡村资源一站式服务', text: '浏览资源、提交合作申请、跟踪审批进度，为你的乡村项目提供更多可能。', button: '查看资源', action: 'map', tone: 'green', photoClass: 'homestay' },
      { kicker: '民宿焕新', title: '闲置农房变身精品民宿', text: '寻找适合改造的院落资源，连接运营团队与村庄空间。', button: '查看民宿', action: 'map', tone: 'earth', photoClass: 'courtyard' },
      { kicker: '研学文旅', title: '把田野变成研学课堂', text: '聚合农田、工坊与乡土文化资源，发现可合作的研学场景。', button: '发现研学', action: 'map', tone: 'gold', photoClass: 'study' },
      { kicker: '产业共创', title: '农产品工坊共建新品牌', text: '对接加工空间、非遗工坊与品牌资源，推动乡村产业落地。', button: '寻找工坊', action: 'match', tone: 'teal', photoClass: 'workshop' }
    ],
    applications: [],
    recentApplications: [],
    stats: [],
    homeMetrics: [],
    risks: [],
    suggestions: [],
    periods: getDashboardPeriods(),
    trendPeriod: '7d',
    trendOption: buildLineOption([])
  },
  onLoad() { this.loadDashboard(); },
  onShow() { if (!this.data.isLoading) this.loadDashboard(); },
  async loadDashboard() {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const [dashboard, applications] = await Promise.all([getDashboardData(), getMyApplications()]);
      const stats = Array.isArray(dashboard.stats) ? dashboard.stats : [];
      const risks = Array.isArray(dashboard.risks) ? dashboard.risks : [];
      const suggestions = Array.isArray(dashboard.suggestions) ? dashboard.suggestions : [];
      const days7 = dashboard.trends && Array.isArray(dashboard.trends.days7) ? dashboard.trends.days7 : [];
      const isEmpty = !stats.length && !risks.length && !suggestions.length && !days7.length && !applications.length;
      this.setData({
        pageState: isEmpty ? PageState.Empty : PageState.Ready,
        isLoading: false,
        roleName: dashboard.roleName || '',
        villageName: dashboard.villageName || '',
        applications,
        recentApplications: applications.slice(0, 3),
        stats,
        homeMetrics: this.buildHomeMetrics(stats, applications),
        risks,
        suggestions,
        trendOption: buildLineOption(days7)
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onRetry() { this.loadDashboard(); },
  onHeroChange(event) { this.setData({ activeHeroIndex: event.detail.current }); },
  buildHomeMetrics(stats = [], applications = []) {
    const pick = (index, fallback) => Number(stats[index] && stats[index].value !== undefined ? stats[index].value : fallback);
    const resources = Math.max(0, pick(0, 17));
    const cooperable = Math.max(0, pick(1, 10));
    const pending = applications.filter((item) => item.status === 'PENDING').length;
    const risk = Math.max(0, pick(3, 1));
    return [
      { title: '可申请资源', value: resources || 17, unit: '项', note: `本周新增 ${Math.max(1, Math.round((resources || 17) * 0.3))} 项`, icon: '叠', tone: 'green', action: 'map' },
      { title: '我的申请', value: applications.length, unit: '条', note: `审核中 ${pending} 条`, icon: '单', tone: 'blue', action: 'collab' },
      { title: '待处理进度', value: Math.max(1, risk), unit: '条', note: '可跟进 1 条', icon: '时', tone: 'orange', action: 'collab' },
      { title: '推荐机会', value: Math.max(6, cooperable), unit: '条', note: '重点推荐', icon: '星', tone: 'violet', action: 'match' }
    ];
  },
  async onTrendPeriodChange(event) {
    try {
      const trendPeriod = event.detail.key;
      const trendData = await getDashboardTrend(trendPeriod);
      this.setData({ trendPeriod, trendOption: buildLineOption(trendData) });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onTipAction(event) {
    const actionType = event.detail.actionType;
    if (actionType === 'forecast') return goForecast();
    if (actionType === 'match') return goInvestmentMatch();
    if (actionType === 'process') return goCollab();
    goReport();
  },
  onEntryTap(event) {
    const action = event.currentTarget.dataset.action;
    if (action === 'map') return goMap();
    if (action === 'collab') return goCollab();
    if (action === 'report') return goReport();
    if (action === 'match') return goInvestmentMatch();
    goForecast();
  }
});
