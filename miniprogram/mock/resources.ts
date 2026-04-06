import { InvestmentMatch, ResourceDetail, ResourcePoint } from '../types';

export const resourceTags = ['全部', '闲置农房', '土地', '文旅空间', '可招商'];

export const resourcePoints: ResourcePoint[] = [
  {
    id: 'res-01',
    name: '青禾驿站院落',
    category: '闲置农房',
    lat: 30.2239,
    lng: 120.1661,
    address: '青禾村南街 18 号',
    area: 680,
    annualEstimate: 82,
    investmentStatus: '可招商',
    tags: ['可招商', '临水景观', '改造成熟'],
    cover: 'https://dummyimage.com/750x420/dce9d2/2f7d32&text=%E9%9D%92%E7%A6%BE%E9%A9%BF%E7%AB%99'
  },
  {
    id: 'res-02',
    name: '稻田研学地块',
    category: '土地',
    lat: 30.2284,
    lng: 120.1708,
    address: '青禾村北侧连片田区',
    area: 2200,
    annualEstimate: 136,
    investmentStatus: '洽谈中',
    tags: ['研学', '亲子农耕', '停车便利'],
    cover: 'https://dummyimage.com/750x420/e8efdb/2f7d32&text=%E7%A8%BB%E7%94%B0%E7%A0%94%E5%AD%A6'
  },
  {
    id: 'res-03',
    name: '古桥文创工坊',
    category: '文旅空间',
    lat: 30.2206,
    lng: 120.1734,
    address: '古桥东侧 6 号仓',
    area: 980,
    annualEstimate: 110,
    investmentStatus: '可招商',
    tags: ['文创', '夜经济', '客流稳定'],
    cover: 'https://dummyimage.com/750x420/f0f2e6/2f7d32&text=%E5%8F%A4%E6%A1%A5%E6%96%87%E5%88%9B'
  },
  {
    id: 'res-04',
    name: '山麓露营草坪',
    category: '文旅空间',
    lat: 30.2188,
    lng: 120.1626,
    address: '青禾村西山麓',
    area: 3500,
    annualEstimate: 158,
    investmentStatus: '已签约',
    tags: ['露营', '活动场地', '已签约'],
    cover: 'https://dummyimage.com/750x420/e7eddd/2f7d32&text=%E9%9C%B2%E8%90%A5%E8%8D%89%E5%9D%AA'
  }
];

export const resourceDetails: Record<string, ResourceDetail> = {
  'res-01': {
    ...resourcePoints[0],
    intro: '原乡宅院改造为复合型体验空间，适合民宿与轻餐结合运营。',
    owner: '青禾村集体经济合作社',
    contact: '王主任 138****2231',
    relatedProjects: ['夜游动线优化', '乡创品牌联名市集'],
    occupancyRate: 62,
    expectedROI: 18
  },
  'res-02': {
    ...resourcePoints[1],
    intro: '连片稻田适合研学营地与四季农事体验，具备停车与道路条件。',
    owner: '青禾农文旅公司',
    contact: '赵经理 139****8821',
    relatedProjects: ['乡村研学线路2.0', '青禾丰收节'],
    occupancyRate: 48,
    expectedROI: 16
  },
  'res-03': {
    ...resourcePoints[2],
    intro: '历史仓体改造空间，适配展陈、手作、轻运营门店组合。',
    owner: '青禾文旅运营中心',
    contact: '陈老师 137****0911',
    relatedProjects: ['古桥夜游试运营', '青年主理人计划'],
    occupancyRate: 55,
    expectedROI: 20
  },
  'res-04': {
    ...resourcePoints[3],
    intro: '山麓开放草坪，已完成基础设施建设，适合露营及节庆活动。',
    owner: '青禾户外合作社',
    contact: '何总 136****6622',
    relatedProjects: ['露营季IP活动'],
    occupancyRate: 70,
    expectedROI: 22
  }
};

export const investmentMatches: Record<string, InvestmentMatch[]> = {
  'res-01': [
    { id: 'm1', investor: '禾谷文旅发展有限公司', score: 92, reason: '与“院落民宿+在地餐饮”模型高度契合，现金流周期短。', priority: '高优先', direction: '精品民宿' },
    { id: 'm2', investor: '拾野生活品牌', score: 86, reason: '擅长县域店态运营，可快速导入年轻客群。', priority: '中优先', direction: '轻餐饮+零售' }
  ],
  'res-02': [
    { id: 'm3', investor: '青芽研学集团', score: 89, reason: '已有农耕课程体系，能快速形成研学产品闭环。', priority: '高优先', direction: '研学营地' }
  ],
  'res-03': [
    { id: 'm4', investor: '原乡造物社', score: 84, reason: '文创内容与空间调性匹配，可共建夜经济IP。', priority: '中优先', direction: '文创工坊' }
  ],
  'res-04': [
    { id: 'm5', investor: '山野露营联盟', score: 80, reason: '活动执行能力强，适合节庆型运营。', priority: '观察', direction: '户外营地' }
  ]
};
