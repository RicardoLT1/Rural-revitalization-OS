import { resourceDetails } from '../../mock/resources';
import { goInvestmentMatch } from '../../utils/navigation';

Page({
  data: {
    detail: resourceDetails['res-01'],
    statusType: 'success'
  },
  onLoad(query: Record<string, string>) {
    const id = query.id || 'res-01';
    const detail = resourceDetails[id] || resourceDetails['res-01'];
    const statusMap: Record<string, string> = {
      '\u53ef\u62db\u5546': 'success',
      '\u6d3d\u8c08\u4e2d': 'warning',
      '\u5df2\u7b7e\u7ea6': 'info'
    };
    this.setData({
      detail,
      statusType: statusMap[detail.investmentStatus] || 'neutral'
    });
  },
  onGoMatch() {
    goInvestmentMatch(this.data.detail.id);
  },
  onMockAction(event: WechatMiniprogram.TouchEvent) {
    const name = event.currentTarget.dataset.name;
    wx.showToast({ title: `${name}\uff08\u6f14\u793a\u6001\uff09`, icon: 'none' });
  }
});