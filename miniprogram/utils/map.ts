import { ResourcePoint } from '../types';

type Marker = WechatMiniprogram.MapMarker;

const markerIconByStatus: Record<ResourcePoint['investmentStatus'], string> = {
  可招商: 'https://dummyimage.com/44x44/2f7d32/ffffff&text=%E6%8B%9B',
  洽谈中: 'https://dummyimage.com/44x44/d58a2a/ffffff&text=%E8%B0%88',
  已签约: 'https://dummyimage.com/44x44/5b6164/ffffff&text=%E7%AD%BE'
};

export const filterResources = (resources: ResourcePoint[], tag: string): ResourcePoint[] => {
  if (tag === '全部') {
    return resources;
  }
  if (tag === '可招商') {
    return resources.filter((item) => item.investmentStatus === '可招商');
  }
  return resources.filter((item) => item.category === tag);
};

export const toMapMarkers = (resources: ResourcePoint[]): Marker[] => {
  return resources.map((item) => ({
    id: Number(item.id.replace('res-', '')),
    latitude: item.lat,
    longitude: item.lng,
    width: 36,
    height: 36,
    callout: {
      content: item.name,
      color: '#2F3437',
      bgColor: '#FFFFFF',
      borderRadius: 8,
      padding: 6,
      display: 'BYCLICK'
    },
    iconPath: markerIconByStatus[item.investmentStatus]
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
