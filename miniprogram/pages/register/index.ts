import { register } from '../../services/auth';

Page({
  data: {
    username: '',
    displayName: '',
    password: '',
    confirmPassword: '',
    isSubmitting: false
  },

  onInput(event: WechatMiniprogram.Input) {
    const key = event.currentTarget.dataset.key;
    if (key) {
      this.setData({ [key]: event.detail.value });
    }
  },

  async onRegister() {
    if (this.data.isSubmitting) {
      return;
    }
    const username = this.data.username.trim();
    const displayName = this.data.displayName.trim() || username;
    const password = this.data.password;
    if (username.length < 3) {
      wx.showToast({ title: '账号至少 3 个字符', icon: 'none' });
      return;
    }
    if (password.length < 6) {
      wx.showToast({ title: '密码至少 6 位', icon: 'none' });
      return;
    }
    if (password !== this.data.confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });
    try {
      await register(username, password, displayName);
      wx.showToast({ title: '注册成功', icon: 'success' });
      setTimeout(() => wx.switchTab({ url: '/pages/resource-map/index' }), 500);
    } catch (error) {
      wx.showToast({ title: (error as { message?: string })?.message || '注册失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
});
