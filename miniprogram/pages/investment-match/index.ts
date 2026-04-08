import { investmentMatches, resourceDetails } from '../../mock/resources';
import { goForecast, goResourceDetail } from '../../utils/navigation';

Page({
  data: {
    resource: resourceDetails['res-01'],
    matches: [] as Array<Record<string, any>>,
    aiSummary: {
      id: 'sum-1',
      title: '\u62db\u5546\u7b56\u7565\u5efa\u8bae',
      content: '\u5efa\u8bae\u4f18\u5148\u63a5\u6d3d\u9ad8\u8bc4\u5206\u5bf9\u8c61\uff0c\u5e76\u5728 7 \u5929\u5185\u5b8c\u6210\u73b0\u573a\u8e0f\u52d8\uff0c\u7f29\u77ed\u8f6c\u5316\u8def\u5f84\u3002',
      priority: 'P1',
      actionLabel: '\u67e5\u770b\u8d8b\u52bf\u9884\u6d4b',
      actionType: 'forecast',
      tag: '\u667a\u80fd\u7b56\u7565'
    }
  },
  onLoad(query: Record<string, string>) {
    const id = query.id || 'res-01';
    const resource = resourceDetails[id] || resourceDetails['res-01'];
    const source = investmentMatches[id] || investmentMatches['res-01'];
    const matches = source.map((item) => ({
      ...item,
      priorityType: item.priority === '\u9ad8\u4f18\u5148' ? 'danger' : item.priority === '\u4e2d\u4f18\u5148' ? 'warning' : 'info'
    }));
    this.setData({ resource, matches });
  },
  onResourceTap() {
    goResourceDetail(this.data.resource.id);
  },
  onAiAction() {
    goForecast();
  }
});
