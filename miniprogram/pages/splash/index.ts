const SPLASH_DURATION = 1800;
let splashTimer: number | undefined;

Page({
  onLoad() {
    console.log('[splash] onLoad: enter splash page');
  },

  onReady() {
    console.log('[splash] onReady: stay for ' + SPLASH_DURATION + 'ms before dashboard');
    splashTimer = setTimeout(() => {
      console.log('[splash] switchTab -> pages/dashboard/index');
      wx.switchTab({ url: '/pages/dashboard/index' });
    }, SPLASH_DURATION) as unknown as number;
  },

  onShow() {
    console.log('[splash] onShow');
  },

  onUnload() {
    if (splashTimer) {
      clearTimeout(splashTimer);
      splashTimer = undefined;
    }
    console.log('[splash] onUnload');
  }
});
