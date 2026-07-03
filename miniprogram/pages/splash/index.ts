import { getCurrentUser } from '../../services/auth';

Page({
  data: {
    loadingText: '正在检查登录状态'
  },

  onReady() {
    this.restoreSession();
  },

  async restoreSession() {
    const token = wx.getStorageSync('XIANGYUN_TOKEN');
    if (!token) {
      this.goLogin();
      return;
    }
    try {
      const user = await getCurrentUser();
      wx.setStorageSync('XIANGYUN_USER', user);
      wx.setStorageSync('XIANGYUN_ROLE', user.role);
      wx.switchTab({ url: '/pages/resource-map/index' });
    } catch (error) {
      wx.removeStorageSync('XIANGYUN_TOKEN');
      wx.removeStorageSync('XIANGYUN_USER');
      wx.removeStorageSync('XIANGYUN_ROLE');
      this.goLogin();
    }
  },

  goLogin() {
    wx.reLaunch({ url: '/pages/login/index' });
  }
});
