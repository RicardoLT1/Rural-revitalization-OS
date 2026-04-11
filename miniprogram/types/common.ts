export type Nullable<T> = T | null;

export type PageLoadState = 'loading' | 'ready' | 'empty' | 'error';

export type StatusType = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

export interface ApiResponse<T> {
  code?: number;
  data?: T;
  message?: string;
  success?: boolean;
}

export interface PageResult<T> {
  list: T[];
  page: number;
  pageSize: number;
  total: number;
}

export interface ApiListResponse<T> extends ApiResponse<PageResult<T>> {}

export interface RequestError {
  code?: number | string;
  message: string;
  statusCode?: number;
  raw?: unknown;
}

export interface OptionItem<TKey extends string = string> {
  key: TKey;
  label: string;
  value?: string | number;
  disabled?: boolean;
}

export interface LabelValuePair<TValue = string | number> {
  label: string;
  value: TValue;
}

export interface BaseCardProps {
  title?: string;
  subtitle?: string;
  icon?: string;
  status?: StatusType;
}

export interface StatusBadgeProps {
  text: string;
  type?: StatusType;
  size?: 'small' | 'medium';
}

export interface ChartCardProps {
  title: string;
  periods?: OptionItem[];
  activePeriod?: string;
  ecOption?: Record<string, unknown>;
}