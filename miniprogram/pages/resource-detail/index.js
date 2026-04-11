const { getResourceDetail } = require('../../services/resource');
const { goInvestmentMatch } = require('../../utils/navigation');

Page({
  data: {
    detail: {},
    statusType: 'success'
  },
  onLoad(query) {
    const id = query.id || 'res-01';
    this.loadDetail(id);
  },
  async loadDetail(id) {
    const detail = await getResourceDetail(id);
    const statusMap = {
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
  onMockAction(event) {
    const name = event.currentTarget.dataset.name;
    wx.showToast({ title: `${name}\uff08\u6f14\u793a\u6001\uff09`, icon: 'none' });
  }
});
