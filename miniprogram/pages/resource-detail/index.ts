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
      可招商: 'success',
      洽谈中: 'warning',
      已签约: 'info'
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
    wx.showToast({ title: `${name}（演示态）`, icon: 'none' });
  }
});
