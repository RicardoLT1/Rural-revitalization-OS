import { isMockMode } from '../config/env';
import { INVESTMENT_STATUS_TYPE } from '../constants/status';
import { investmentMatches, resourceDetails, resourcePoints, resourceTags } from '../mock/resources';
import type { StatusType } from '../types/common';
import type { InvestmentMatch, ResourceDetail, ResourceMapView, ResourcePoint } from '../types/resource';
import { get } from '../utils/request';
import { calcMapCenter, filterResources, toMapMarkers } from '../utils/map';

export const DEFAULT_RESOURCE_ID = 'res-01';

export const getResourceTags = async (): Promise<string[]> => {
  if (isMockMode()) {
    return resourceTags;
  }
  return get<string[]>('/resources/tags');
};

export const getResources = async (): Promise<ResourcePoint[]> => {
  if (isMockMode()) {
    return resourcePoints;
  }
  return get<ResourcePoint[]>('/resources');
};

export const getResourceDetail = async (id = DEFAULT_RESOURCE_ID): Promise<ResourceDetail> => {
  if (isMockMode()) {
    return resourceDetails[id] || resourceDetails[DEFAULT_RESOURCE_ID];
  }
  return get<ResourceDetail>(`/resources/${id}`);
};

export const getInvestmentMatches = async (resourceId = DEFAULT_RESOURCE_ID): Promise<InvestmentMatch[]> => {
  if (isMockMode()) {
    return investmentMatches[resourceId] || investmentMatches[DEFAULT_RESOURCE_ID];
  }
  return get<InvestmentMatch[]>(`/resources/${resourceId}/investment-matches`);
};

export const getInvestmentStatusType = (status: string): StatusType => {
  return INVESTMENT_STATUS_TYPE[status] || 'neutral';
};

export const getResourceMapView = async (activeTag = '\u5168\u90e8'): Promise<ResourceMapView> => {
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
