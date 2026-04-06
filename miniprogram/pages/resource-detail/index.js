const { resourceDetails } = require('../../mock/resources');
const { goInvestmentMatch } = require('../../utils/navigation');

Page({
  data: {
    detail: resourceDetails['res-01'],
    statusType: 'success'
  },
  onLoad(query) {
    const id = query.id || 'res-01';
    const detail = resourceDetails[id] || resourceDetails['res-01'];
    const statusMap = {
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
  onMockAction(event) {
    const name = event.currentTarget.dataset.name;
    wx.showToast({ title: `${name}（演示态）`, icon: 'none' });
  }
});