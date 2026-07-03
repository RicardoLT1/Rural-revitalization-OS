import { isMockMode } from '../config/env';
import { INVESTMENT_STATUS_TYPE } from '../constants/status';
import { investmentMatches, resourceDetails, resourcePoints, resourceTags } from '../mock/resources';
import type { StatusType } from '../types/common';
import type { InvestmentMatch, ResourceDetail, ResourceMapView, ResourcePoint } from '../types/resource';
import { get } from '../utils/request';
import { calcMapCenter, filterResources, toMapMarkers } from '../utils/map';

export const DEFAULT_RESOURCE_ID = 'res-01';
export const DEFAULT_API_RESOURCE_ID = '101';

export interface ResourceQuery {
  page?: number;
  size?: number;
  keyword?: string;
  category?: string;
  status?: string;
}

const isAll = (value?: string) => !value || value === '全部' || value === 'ALL';

const unwrapList = <T>(payload: unknown): T[] => {
  if (Array.isArray(payload)) return payload as T[];
  const source = payload as Record<string, unknown> | undefined;
  if (!source || typeof source !== 'object') return [];
  if (Array.isArray(source.items)) return source.items as T[];
  if (Array.isArray(source.records)) return source.records as T[];
  if (Array.isArray(source.list)) return source.list as T[];
  if (Array.isArray(source.rows)) return source.rows as T[];
  if (source.data) return unwrapList<T>(source.data);
  return [];
};

const getMockResources = (query: ResourceQuery = {}): ResourcePoint[] => {
  const keyword = query.keyword?.trim();
  return resourcePoints.filter((item) => {
    const matchKeyword = !keyword || item.name.includes(keyword) || item.address.includes(keyword);
    const matchCategory = isAll(query.category) || item.category === query.category;
    const matchStatus = isAll(query.status) || item.investmentStatus === query.status;
    return matchKeyword && matchCategory && matchStatus;
  });
};

export const getResourceTags = async (): Promise<string[]> => {
  if (isMockMode('resource')) return resourceTags;
  try {
    const payload = await get<unknown>('/resource-tags');
    const tags = unwrapList<string>(payload).filter(Boolean);
    return tags.length ? tags : resourceTags;
  } catch (error) {
    return resourceTags;
  }
};

export const getResources = async (query: ResourceQuery = {}): Promise<ResourcePoint[]> => {
  if (isMockMode('resource')) return getMockResources(query);
  try {
    const payload = await get<unknown>('/resources', {
      page: query.page || 1,
      size: query.size || 10,
      keyword: query.keyword || '',
      category: isAll(query.category) ? '' : query.category,
      status: isAll(query.status) ? '' : query.status
    });
    const list = unwrapList<ResourcePoint>(payload);
    return list.length ? list : getMockResources(query);
  } catch (error) {
    return getMockResources(query);
  }
};

export const getResourceDetail = async (id?: string): Promise<ResourceDetail> => {
  if (isMockMode('resource')) {
    const targetId = id || DEFAULT_RESOURCE_ID;
    return resourceDetails[targetId] || resourceDetails[DEFAULT_RESOURCE_ID];
  }
  const targetId = id && !id.startsWith('res-') ? id : DEFAULT_API_RESOURCE_ID;
  try {
    return await get<ResourceDetail>(`/resources/${targetId}`);
  } catch (error) {
    return resourceDetails[id || DEFAULT_RESOURCE_ID] || resourceDetails[DEFAULT_RESOURCE_ID];
  }
};

export const getInvestmentMatches = async (resourceId = DEFAULT_RESOURCE_ID): Promise<InvestmentMatch[]> => {
  if (isMockMode('resource')) return investmentMatches[resourceId] || investmentMatches[DEFAULT_RESOURCE_ID];
  const targetId = resourceId && !resourceId.startsWith('res-') ? resourceId : DEFAULT_API_RESOURCE_ID;
  try {
    const view = await get<{ matches: InvestmentMatch[] }>('/investment-matches', { resourceId: targetId });
    return view.matches || [];
  } catch (error) {
    return investmentMatches[resourceId] || investmentMatches[DEFAULT_RESOURCE_ID];
  }
};

export const getInvestmentStatusType = (status: string): StatusType => {
  return INVESTMENT_STATUS_TYPE[status] || 'neutral';
};

export const getResourceMapView = async (activeTag = '全部'): Promise<ResourceMapView> => {
  const [tags, resources] = await Promise.all([getResourceTags(), getResources()]);
  const filteredResources = filterResources(resources, activeTag);
  return {
    tagOptions: tags.map((tag) => ({ key: tag, label: tag })),
    activeTag,
    allResources: resources,
    filteredResources,
    selectedResource: filteredResources[0] || {},
    markers: toMapMarkers(filteredResources),
    mapCenter: calcMapCenter(filteredResources)
  };
};