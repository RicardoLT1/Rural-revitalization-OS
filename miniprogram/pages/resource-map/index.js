const { resourcePoints, resourceTags } = require('../../mock/resources');
const { filterResources, toMapMarkers, calcMapCenter } = require('../../utils/map');
const { goResourceDetail } = require('../../utils/navigation');

const formatTagOptions = resourceTags.map((tag) => ({ key: tag, label: tag }));

Page({
  data: {
    tagOptions: formatTagOptions,
    activeTag: '全部',
    allResources: resourcePoints,
    filteredResources: resourcePoints,
    selectedResource: {},
    markers: toMapMarkers(resourcePoints),
    mapCenter: calcMapCenter(resourcePoints)
  },
  onTagChange(event) {
    const activeTag = event.detail.key;
    const filteredResources = filterResources(resourcePoints, activeTag);
    this.setData({
      activeTag,
      filteredResources,
      selectedResource: filteredResources[0] || {},
      markers: toMapMarkers(filteredResources),
      mapCenter: calcMapCenter(filteredResources)
    });
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
  onLoad() {
    this.setData({ selectedResource: resourcePoints[0] });
  }
});