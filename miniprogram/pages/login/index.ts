import { getCurrentUser, login } from '../../services/auth';

Page({
  data: {
    username: '',
    password: '',
    isSubmitting: false
  },

  onLoad() {
    if (wx.getStorageSync('XIANGYUN_TOKEN')) {
      getCurrentUser()
        .then(() => this.goHome())
        .catch(() => this.clearSession());
    }
  },

  onUsernameInput(event: WechatMiniprogram.Input) {
    this.setData({ username: event.detail.value.trim() });
  },

  onPasswordInput(event: WechatMiniprogram.Input) {
    this.setData({ password: event.detail.value });
  },

  async onLogin() {
    if (this.data.isSubmitting) {
      return;
    }
    if (!this.data.username || !this.data.password) {
      wx.showToast({ title: '请输入账号和密码', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });
    try {
      await login(this.data.username, this.data.password);
      this.goHome();
    } catch (error) {
      wx.showToast({ title: this.messageOf(error, '登录失败，请检查账号和密码'), icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  onGoRegister() {
    wx.navigateTo({ url: '/pages/register/index' });
  },

  goHome() {
    wx.switchTab({ url: '/pages/resource-map/index' });
  },

  clearSession() {
    wx.removeStorageSync('XIANGYUN_TOKEN');
    wx.removeStorageSync('XIANGYUN_USER');
    wx.removeStorageSync('XIANGYUN_ROLE');
  },

  messageOf(error: unknown, fallback: string) {
    const message = (error as { message?: string })?.message || fallback;
    if (message.includes('停用')) {
      return '账号已停用，请联系工作人员';
    }
    if (message.includes('Network') || message.includes('request')) {
      return '网络异常，请稍后重试';
    }
    return message;
  }
});
