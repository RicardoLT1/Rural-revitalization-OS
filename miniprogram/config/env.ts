export type DataSource = 'mock' | 'api';
export type ApiModule = 'dashboard' | 'resource' | 'workflow' | 'report';

export const envConfig = {
  dataSource: 'api' as DataSource,
  baseURL: 'http://127.0.0.1:8080/api',
  demoAutoLogin: false,
  demoAccount: {
    username: 'user_demo',
    password: '123456'
  },
  timeout: 10000,
  enableDebugLog: false,
  apiModules: {
    dashboard: true,
    resource: true,
    workflow: true,
    report: true
  } as Record<ApiModule, boolean>
};

export const isMockMode = (module?: ApiModule) => {
  if (envConfig.dataSource === 'mock') {
    return true;
  }
  return module ? !envConfig.apiModules[module] : false;
};
