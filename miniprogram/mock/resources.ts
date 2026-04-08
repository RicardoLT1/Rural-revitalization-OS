import { InvestmentMatch, ResourceDetail, ResourcePoint } from '../types';

export const resourceTags = ['\u5168\u90e8', '\u95f2\u7f6e\u519c\u623f', '\u571f\u5730', '\u6587\u65c5\u7a7a\u95f4', '\u53ef\u62db\u5546'];

export const resourcePoints: ResourcePoint[] = [
  {
    id: 'res-01',
    name: '\u9752\u79be\u9a7f\u7ad9\u9662\u843d',
    category: '\u95f2\u7f6e\u519c\u623f',
    lat: 30.2239,
    lng: 120.1661,
    address: '\u9752\u79be\u6751\u5357\u8857 18 \u53f7',
    area: 680,
    annualEstimate: 82,
    investmentStatus: '\u53ef\u62db\u5546',
    tags: ['\u4e34\u6c34\u666f\u89c2', '\u6539\u9020\u6210\u719f', '\u4ea4\u901a\u4fbf\u5229']
  },
  {
    id: 'res-02',
    name: '\u7a3b\u7530\u7814\u5b66\u5730\u5757',
    category: '\u571f\u5730',
    lat: 30.2284,
    lng: 120.1708,
    address: '\u9752\u79be\u6751\u5317\u4fa7\u8fde\u7247\u7530\u533a',
    area: 2200,
    annualEstimate: 136,
    investmentStatus: '\u6d3d\u8c08\u4e2d',
    tags: ['\u7814\u5b66\u8425\u5730', '\u4eb2\u5b50\u519c\u8015', '\u505c\u8f66\u4fbf\u5229']
  },
  {
    id: 'res-03',
    name: '\u53e4\u6865\u6587\u521b\u5de5\u574a',
    category: '\u6587\u65c5\u7a7a\u95f4',
    lat: 30.2206,
    lng: 120.1734,
    address: '\u53e4\u6865\u4e1c\u4fa7 6 \u53f7\u4ed3',
    area: 980,
    annualEstimate: 110,
    investmentStatus: '\u53ef\u62db\u5546',
    tags: ['\u6587\u521b\u4e1a\u6001', '\u591c\u7ecf\u6d4e', '\u5ba2\u6d41\u7a33\u5b9a']
  },
  {
    id: 'res-04',
    name: '\u5c71\u9e93\u9732\u8425\u8349\u576a',
    category: '\u6587\u65c5\u7a7a\u95f4',
    lat: 30.2188,
    lng: 120.1626,
    address: '\u9752\u79be\u6751\u897f\u5c71\u9e93',
    area: 3500,
    annualEstimate: 158,
    investmentStatus: '\u5df2\u7b7e\u7ea6',
    tags: ['\u9732\u8425\u6d3b\u52a8', '\u8282\u5e86\u573a\u5730', '\u666f\u89c2\u5f00\u9614']
  }
];

export const resourceDetails: Record<string, ResourceDetail> = {
  'res-01': {
    ...resourcePoints[0],
    intro: '\u539f\u4e61\u5b85\u9662\u6539\u9020\u4e3a\u590d\u5408\u4f53\u9a8c\u7a7a\u95f4\uff0c\u9002\u5408\u6c11\u5bbf\u4e0e\u8f7b\u9910\u996e\u7ed3\u5408\u8fd0\u8425\u3002',
    owner: '\u9752\u79be\u6751\u96c6\u4f53\u7ecf\u6d4e\u5408\u4f5c\u793e',
    contact: '\u738b\u4e3b\u4efb 138****2231',
    relatedProjects: ['\u591c\u6e38\u52a8\u7ebf\u4f18\u5316', '\u4e61\u521b\u54c1\u724c\u8054\u540d\u5e02\u96c6'],
    occupancyRate: 62,
    expectedROI: 18
  },
  'res-02': {
    ...resourcePoints[1],
    intro: '\u8fde\u7247\u7a3b\u7530\u9002\u5408\u7814\u5b66\u8425\u5730\u4e0e\u56db\u5b63\u519c\u4e8b\u4f53\u9a8c\uff0c\u5177\u5907\u505c\u8f66\u548c\u9053\u8def\u6761\u4ef6\u3002',
    owner: '\u9752\u79be\u519c\u6587\u65c5\u516c\u53f8',
    contact: '\u8d75\u7ecf\u7406 139****8821',
    relatedProjects: ['\u4e61\u6751\u7814\u5b66\u7ebf\u8def 2.0', '\u9752\u79be\u4e30\u6536\u8282'],
    occupancyRate: 48,
    expectedROI: 16
  },
  'res-03': {
    ...resourcePoints[2],
    intro: '\u5386\u53f2\u4ed3\u4f53\u6539\u9020\u7a7a\u95f4\uff0c\u9002\u914d\u5c55\u9648\u3001\u624b\u4f5c\u4e0e\u8f7b\u8fd0\u8425\u95e8\u5e97\u7ec4\u5408\u3002',
    owner: '\u9752\u79be\u6587\u65c5\u8fd0\u8425\u4e2d\u5fc3',
    contact: '\u9648\u8001\u5e08 137****0911',
    relatedProjects: ['\u53e4\u6865\u591c\u6e38\u8bd5\u8fd0\u8425', '\u9752\u5e74\u4e3b\u7406\u4eba\u8ba1\u5212'],
    occupancyRate: 55,
    expectedROI: 20
  },
  'res-04': {
    ...resourcePoints[3],
    intro: '\u5c71\u9e93\u5f00\u653e\u8349\u576a\u5df2\u5b8c\u6210\u57fa\u7840\u8bbe\u65bd\u5efa\u8bbe\uff0c\u9002\u5408\u9732\u8425\u4e0e\u8282\u5e86\u6d3b\u52a8\u3002',
    owner: '\u9752\u79be\u6237\u5916\u5408\u4f5c\u793e',
    contact: '\u4f55\u603b 136****6622',
    relatedProjects: ['\u9732\u8425\u5b63 IP \u6d3b\u52a8'],
    occupancyRate: 70,
    expectedROI: 22
  }
};

export const investmentMatches: Record<string, InvestmentMatch[]> = {
  'res-01': [
    { id: 'm1', investor: '\u79be\u8c37\u6587\u65c5\u53d1\u5c55\u6709\u9650\u516c\u53f8', score: 92, reason: '\u4e0e\u201c\u9662\u843d\u6c11\u5bbf+\u5728\u5730\u9910\u996e\u201d\u6a21\u578b\u9ad8\u5ea6\u5951\u5408\uff0c\u73b0\u91d1\u6d41\u5468\u671f\u77ed\u3002', priority: '\u9ad8\u4f18\u5148', direction: '\u7cbe\u54c1\u6c11\u5bbf' },
    { id: 'm2', investor: '\u62fe\u91ce\u751f\u6d3b\u54c1\u724c', score: 86, reason: '\u64c5\u957f\u53bf\u57df\u5e97\u6001\u8fd0\u8425\uff0c\u53ef\u5feb\u901f\u5bfc\u5165\u5e74\u8f7b\u5ba2\u7fa4\u3002', priority: '\u4e2d\u4f18\u5148', direction: '\u8f7b\u9910\u996e+\u96f6\u552e' }
  ],
  'res-02': [
    { id: 'm3', investor: '\u9752\u82bd\u7814\u5b66\u96c6\u56e2', score: 89, reason: '\u5df2\u6709\u519c\u8015\u8bfe\u7a0b\u4f53\u7cfb\uff0c\u53ef\u5feb\u901f\u5f62\u6210\u7814\u5b66\u4ea7\u54c1\u95ed\u73af\u3002', priority: '\u9ad8\u4f18\u5148', direction: '\u7814\u5b66\u8425\u5730' }
  ],
  'res-03': [
    { id: 'm4', investor: '\u539f\u4e61\u9020\u7269\u793e', score: 84, reason: '\u6587\u521b\u5185\u5bb9\u4e0e\u7a7a\u95f4\u8c03\u6027\u5339\u914d\uff0c\u53ef\u5171\u5efa\u591c\u7ecf\u6d4e IP\u3002', priority: '\u4e2d\u4f18\u5148', direction: '\u6587\u521b\u5de5\u574a' }
  ],
  'res-04': [
    { id: 'm5', investor: '\u5c71\u91ce\u9732\u8425\u8054\u76df', score: 80, reason: '\u6d3b\u52a8\u6267\u884c\u80fd\u529b\u5f3a\uff0c\u9002\u5408\u8282\u5e86\u578b\u8fd0\u8425\u3002', priority: '\u89c2\u5bdf', direction: '\u6237\u5916\u8425\u5730' }
  ]
};