import type { ApiModule, DataSource } from './env';

export const envExample = {
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
