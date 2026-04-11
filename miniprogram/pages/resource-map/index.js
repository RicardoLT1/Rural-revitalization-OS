const { getResourceMapView } = require('../../services/resource');
const { goResourceDetail } = require('../../utils/navigation');

Page({
  data: {
    tagOptions: [],
    activeTag: '\u5168\u90e8',
    allResources: [],
    filteredResources: [],
    selectedResource: {},
    markers: [],
    mapCenter: { latitude: 30.2239, longitude: 120.1661 }
  },
  onLoad() {
    this.loadResourceMap(this.data.activeTag);
  },
  onTagChange(event) {
    this.loadResourceMap(event.detail.key);
  },
  async loadResourceMap(activeTag) {
    const view = await getResourceMapView(activeTag);
    this.setData(view);
  },
  onMarkerTap(event) {
    const markerId = event.detail.markerId;
    const selectedResource = this.data.filteredResources.find((item) => Number(item.id.replace('res-', '')) === markerId);
    if (selectedResource) {
      this.setData({ selectedResource });
    }
  },
  onResourceTap(event) {
    goResourceDetail(event.detail.id);
  },
  onPopupTap(event) {
    goResourceDetail(event.detail.id);
  },
});
