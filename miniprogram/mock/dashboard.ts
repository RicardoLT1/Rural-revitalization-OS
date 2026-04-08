import { DashboardMetrics } from '../types';

export const dashboardData: DashboardMetrics = {
  villageName: '\u9752\u79be\u793a\u8303\u6751',
  roleName: '\u4e61\u6751CEO',
  stats: [
    { key: 'flow', title: '\u4eca\u65e5\u5ba2\u6d41', value: 18640, unit: '\u4eba\u6b21', delta: 12.4, trend: 'up', status: 'success', icon: '\u5ba2' },
    { key: 'revenue', title: '\u672c\u6708\u8425\u6536', value: 268.7, unit: '\u4e07\u5143', delta: 8.6, trend: 'up', status: 'info', icon: '\u6536' },
    { key: 'progress', title: '\u9879\u76ee\u8fdb\u5ea6', value: 74, unit: '%', delta: 4.2, trend: 'up', status: 'success', icon: '\u8fdb' },
    { key: 'risk', title: '\u98ce\u9669\u9884\u8b66', value: 3, unit: '\u9879', delta: -1, trend: 'down', status: 'warning', icon: '\u8b66' }
  ],
  trends: {
    days7: [
      { date: '03-30', value: 13200 },
      { date: '03-31', value: 14120 },
      { date: '04-01', value: 14980 },
      { date: '04-02', value: 16100 },
      { date: '04-03', value: 15800 },
      { date: '04-04', value: 17260 },
      { date: '04-05', value: 18640 }
    ],
    days30: [
      { date: '\u7b2c1\u5468', value: 85200 },
      { date: '\u7b2c2\u5468', value: 90600 },
      { date: '\u7b2c3\u5468', value: 97800 },
      { date: '\u7b2c4\u5468', value: 103600 }
    ]
  },
  risks: [
    { id: 'r1', title: '\u53e4\u6865\u7247\u533a\u6392\u6c34\u9690\u60a3', level: 'high', detail: '\u8fde\u7eed\u964d\u96e8\u540e\u5c40\u90e8\u8def\u6bb5\u5b58\u5728\u79ef\u6c34\u98ce\u9669\u3002', assignee: '\u5de5\u7a0b\u534f\u540c\u7ec4' },
    { id: 'r2', title: '\u6c11\u5bbf\u5ba1\u6279\u8282\u70b9\u5ef6\u8fdf', level: 'medium', detail: '\u5ba1\u6279\u5df2\u6ede\u540e 2 \u5929\uff0c\u5efa\u8bae\u4f18\u5148\u5904\u7406\u3002', assignee: '\u8fd0\u8425\u5ba1\u6279\u7ec4' },
    { id: 'r3', title: '\u5468\u672b\u505c\u8f66\u627f\u8f7d\u9884\u8b66', level: 'low', detail: '\u9884\u6d4b\u5468\u672b\u8f66\u6d41\u4e0a\u6d6e\u7ea6 15%\u3002', assignee: '\u73b0\u573a\u8c03\u5ea6\u7ec4' }
  ],
  suggestions: [
    {
      id: 'a1',
      title: '\u4f18\u5316\u591c\u6e38\u5165\u573a\u65f6\u6bb5',
      content: '\u5efa\u8bae\u5c06\u591c\u6e38\u6d3b\u52a8\u63d0\u524d\u81f3 18:30\uff0c\u7f13\u89e3\u4e3b\u8857\u62e5\u5835\u5e76\u63d0\u5347\u505c\u7559\u65f6\u957f\u3002',
      priority: 'P1',
      actionLabel: '\u67e5\u770b\u8d8b\u52bf\u9884\u6d4b',
      actionType: 'forecast',
      tag: '\u5ba2\u6d41\u8c03\u5ea6'
    },
    {
      id: 'a2',
      title: '\u4f18\u5148\u63a8\u8fdb\u519c\u623f\u62db\u5546',
      content: 'A03\u3001A07 \u8d44\u6e90\u4e0e\u6587\u65c5\u8f7b\u9910\u6295\u8d44\u65b9\u5339\u914d\u5ea6\u8d85\u8fc7 85%\uff0c\u53ef\u4f18\u5148\u63a8\u8fdb\u3002',
      priority: 'P2',
      actionLabel: '\u67e5\u770b\u62db\u5546\u63a8\u8350',
      actionType: 'match',
      tag: '\u62db\u5546\u52a0\u901f'
    },
    {
      id: 'a3',
      title: '\u590d\u76d8\u5ba1\u6279\u74f6\u9888\u8282\u70b9',
      content: '\u8d44\u4ea7\u6d41\u8f6c\u6d41\u7a0b\u5728\u201c\u4e61\u9547\u590d\u6838\u201d\u8282\u70b9\u5e73\u5747\u505c\u7559 1.8 \u5929\uff0c\u5efa\u8bae\u5e76\u884c\u8865\u4ef6\u3002',
      priority: 'P2',
      actionLabel: '\u67e5\u770b\u534f\u540c\u5de5\u4f5c\u53f0',
      actionType: 'process',
      tag: '\u6d41\u7a0b\u4f18\u5316'
    }
  ]
};