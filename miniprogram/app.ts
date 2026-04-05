/// <reference path="./types/wx.d.ts" />

App<IAppOption>({
  globalData: {
    roleName: '乡村CEO',
    villageName: '青禾示范村'
  },
  onLaunch() {
    // 原型版不接后端，仅保留本地初始化入口。
  }
})
