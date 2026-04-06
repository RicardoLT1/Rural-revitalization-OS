import { resourcePoints, resourceTags } from '../../mock/resources';
import { ResourcePoint } from '../../types';
import { filterResources, toMapMarkers, calcMapCenter } from '../../utils/map';
import { goResourceDetail } from '../../utils/navigation';

const formatTagOptions = resourceTags.map((tag) => ({ key: tag, label: tag }));

Page({
  data: {
    tagOptions: formatTagOptions,
    activeTag: '全部',
    allResources: resourcePoints,
    filteredResources: resourcePoints,
    selectedResource: {} as Partial<ResourcePoint>,
    markers: toMapMarkers(resourcePoints),
    mapCenter: calcMapCenter(resourcePoints)
  },
  onTagChange(event: WechatMiniprogram.CustomEvent<{ key: string }>) {
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
  },
  onLoad() {
    this.setData({ selectedResource: resourcePoints[0] });
  }
});
