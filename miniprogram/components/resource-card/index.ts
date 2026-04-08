Component({
  properties: {
    resource: { type: Object, value: {} },
    showActions: { type: Boolean, value: false },
    active: { type: Boolean, value: false }
  },
  data: {
    statusType: 'neutral',
    coverClass: 'house',
    coverDesc: '\u9662\u843d\u6539\u9020\u6f5c\u529b\u9ad8\uff0c\u9002\u914d\u6c11\u5bbf\u4e0e\u8f7b\u9910\u996e\u8fd0\u8425'
  },
  observers: {
    'resource.investmentStatus': function (status: string) {
      const map: Record<string, string> = {
        '\u53ef\u62db\u5546': 'success',
        '\u6d3d\u8c08\u4e2d': 'warning',
        '\u5df2\u7b7e\u7ea6': 'info'
      };
      this.setData({ statusType: map[status] || 'neutral' });
    },
    'resource.category': function (category: string) {
      const coverMap: Record<string, string> = {
        '\u95f2\u7f6e\u519c\u623f': 'house',
        '\u571f\u5730': 'land',
        '\u6587\u65c5\u7a7a\u95f4': 'culture'
      };
      const descMap: Record<string, string> = {
        '\u95f2\u7f6e\u519c\u623f': '\u9662\u843d\u6539\u9020\u6f5c\u529b\u9ad8\uff0c\u9002\u914d\u6c11\u5bbf\u4e0e\u8f7b\u9910\u996e\u8fd0\u8425',
        '\u571f\u5730': '\u8fde\u7247\u5730\u5757\u53ef\u505a\u7814\u5b66\u8425\u5730\u4e0e\u519c\u4e8b\u4f53\u9a8c',
        '\u6587\u65c5\u7a7a\u95f4': '\u9002\u5408\u6587\u521b\u5c55\u9648\u3001\u591c\u6e38\u6d3b\u52a8\u4e0e\u54c1\u724c\u8054\u8425'
      };
      this.setData({
        coverClass: coverMap[category] || 'house',
        coverDesc: descMap[category] || '\u8d44\u6e90\u57fa\u7840\u6761\u4ef6\u5b8c\u5584\uff0c\u53ef\u5feb\u901f\u542f\u52a8\u62db\u5546\u5bf9\u63a5'
      });
    }
  },
  methods: {
    onTap() {
      const current = this.properties.resource as { id?: string };
      this.triggerEvent('tap', { id: current.id });
    },
    onAction(event: WechatMiniprogram.TouchEvent) {
      const action = event.currentTarget.dataset.action;
      const current = this.properties.resource as { id?: string };
      this.triggerEvent(action, { id: current.id });
    }
  }
});