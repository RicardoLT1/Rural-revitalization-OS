import { OptionItem, StatusType } from './common';

export type ResourceCategory = '\u95f2\u7f6e\u519c\u623f' | '\u571f\u5730' | '\u6587\u65c5\u7a7a\u95f4';
export type ResourceStatus = '\u53ef\u62db\u5546' | '\u6d3d\u8c08\u4e2d' | '\u5df2\u7b7e\u7ea6';
export type InvestmentStatus = ResourceStatus;
export type ResourceTag = string;

export interface InvestmentTag {
  text: string;
  type?: StatusType;
}

export interface ResourcePoint {
  id: string;
  name: string;
  category: ResourceCategory;
  lat: number;
  lng: number;
  address: string;
  area: number;
  annualEstimate: number;
  investmentStatus: InvestmentStatus;
  tags: ResourceTag[];
}

export interface ResourceCard extends ResourcePoint {
  coverDesc?: string;
  statusType?: StatusType;
}

export interface ResourceDetail extends ResourcePoint {
  intro: string;
  owner: string;
  contact: string;
  relatedProjects: string[];
  occupancyRate: number;
  expectedROI: number;
}

export interface InvestmentMatch {
  id: string;
  investor: string;
  score: number;
  reason: string;
  priority: string;
  direction: string;
}

export interface InvestmentMatchViewItem extends InvestmentMatch {
  priorityType: StatusType;
}

export interface ResourceMapView {
  tagOptions: OptionItem[];
  activeTag: string;
  allResources: ResourcePoint[];
  filteredResources: ResourcePoint[];
  selectedResource: Partial<ResourcePoint>;
  markers: WechatMiniprogram.MapMarker[];
  mapCenter: {
    latitude: number;
    longitude: number;
  };
}