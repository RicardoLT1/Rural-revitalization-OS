import { DEFAULT_LOADING_TEXT, PageState, getErrorMessage } from '../../constants/page';
import { getResourceMapView } from '../../services/resource';
import type { PageLoadState } from '../../types/common';
import type { ResourcePoint } from '../../types/resource';
import { goResourceDetail } from '../../utils/navigation';

Page({
  data: {
    pageState: PageState.Loading as PageLoadState,
    isLoading: true,
    errorMessage: '',
    loadingText: DEFAULT_LOADING_TEXT,
    errorTitle: '\u8d44\u6e90\u5730\u56fe\u52a0\u8f7d\u5931\u8d25',
    emptyTitle: '\u6682\u65e0\u8d44\u6e90\u70b9\u4f4d',
    emptyDescription: '\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u4e0b\u6682\u65e0\u53ef\u5c55\u793a\u7684\u4e61\u6751\u8d44\u6e90\u3002',
    tagOptions: [],
    activeTag: '\u5168\u90e8',
    allResources: [] as ResourcePoint[],
    filteredResources: [] as ResourcePoint[],
    selectedResource: {} as Partial<ResourcePoint>,
    markers: [],
    mapCenter: { latitude: 30.2239, longitude: 120.1661 }
  },
  onLoad() {
    this.loadResourceMap(this.data.activeTag);
  },
  onRetry() {
    this.loadResourceMap(this.data.activeTag);
  },
  onTagChange(event: WechatMiniprogram.CustomEvent<{ key: string }>) {
    this.loadResourceMap(event.detail.key);
  },
  async loadResourceMap(activeTag: string) {
    this.setData({ pageState: PageState.Loading, isLoading: true, errorMessage: '' });
    try {
      const view = await getResourceMapView(activeTag);
      this.setData({
        ...view,
        pageState: view.filteredResources.length ? PageState.Ready : PageState.Empty,
        isLoading: false
      });
    } catch (error) {
      this.setData({ pageState: PageState.Error, isLoading: false, errorMessage: getErrorMessage(error) });
    }
  },
  onMarkerTap(event: WechatMiniprogram.CustomEvent<{ markerId: number }>) {
    const markerId = event.detail.markerId;
    const selectedResource = this.data.filteredResources.find((item) => Number(item.id.replace('res-', '')) === markerId);
    if (selectedResource) {
      this.setData({ selectedResource });
    }
  },
  onResourceTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    goResourceDetail(event.detail.id);
  },
  onPopupTap(event: WechatMiniprogram.CustomEvent<{ id: string }>) {
    goResourceDetail(event.detail.id);
  }
});
