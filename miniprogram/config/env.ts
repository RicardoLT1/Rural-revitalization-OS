export type DataSource = 'mock' | 'api';

export const envConfig = {
  dataSource: 'mock' as DataSource,
  baseURL: '',
  timeout: 10000,
  enableDebugLog: false
};

export const isMockMode = () => envConfig.dataSource === 'mock';
