import { ResourcePoint } from '../types';

type Marker = WechatMiniprogram.MapMarker;

export const filterResources = (resources: ResourcePoint[], tag: string): ResourcePoint[] => {
  if (tag === '\u5168\u90e8') {
    return resources;
  }
  if (tag === '\u53ef\u62db\u5546') {
    return resources.filter((item) => item.investmentStatus === '\u53ef\u62db\u5546');
  }
  return resources.filter((item) => item.category === tag);
};

const markerColorByStatus: Record<ResourcePoint['investmentStatus'], string> = {
  '\u53ef\u62db\u5546': '#2F7D32',
  '\u6d3d\u8c08\u4e2d': '#D58A2A',
  '\u5df2\u7b7e\u7ea6': '#5B6164'
};

export const toMapMarkers = (resources: ResourcePoint[]): Marker[] => {
  const statusLabelMap: Record<ResourcePoint['investmentStatus'], string> = {
    '\u53ef\u62db\u5546': '\u62db',
    '\u6d3d\u8c08\u4e2d': '\u8c08',
    '\u5df2\u7b7e\u7ea6': '\u7b7e'
  };

  return resources.map((item) => ({
    id: Number(item.id.replace('res-', '')),
    latitude: item.lat,
    longitude: item.lng,
    width: 32,
    height: 32,
    alpha: 1,
    callout: {
      content: `${item.name} | ${item.category}`,
      color: '#2F3437',
      bgColor: '#FFFFFF',
      borderColor: markerColorByStatus[item.investmentStatus],
      borderWidth: 1,
      borderRadius: 12,
      padding: 8,
      display: 'BYCLICK'
    },
    label: {
      content: statusLabelMap[item.investmentStatus],
      color: '#FFFFFF',
      fontSize: 12,
      bgColor: markerColorByStatus[item.investmentStatus],
      borderRadius: 999,
      padding: 4,
      anchorX: 16,
      anchorY: -14
    }
  }));
};

export const calcMapCenter = (resources: ResourcePoint[]): { latitude: number; longitude: number } => {
  if (!resources.length) {
    return { latitude: 30.2239, longitude: 120.1661 };
  }
  const lat = resources.reduce((sum, item) => sum + item.lat, 0) / resources.length;
  const lng = resources.reduce((sum, item) => sum + item.lng, 0) / resources.length;
  return { latitude: Number(lat.toFixed(6)), longitude: Number(lng.toFixed(6)) };
};