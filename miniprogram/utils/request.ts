import { envConfig } from '../config/env';
import type { ApiResponse, RequestError } from '../types/common';

export type Method = 'GET' | 'POST' | 'PUT' | 'DELETE';

export interface RequestOptions<T = unknown> {
  url: string;
  method?: Method;
  data?: T;
  header?: Record<string, string>;
  timeout?: number;
}

const buildUrl = (url: string) => {
  if (/^https?:\/\//.test(url)) {
    return url;
  }
  return `${envConfig.baseURL}${url}`;
};

const getAuthHeader = (): Record<string, string> => {
  const token = wx.getStorageSync('XIANGYUN_TOKEN');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

let demoLoginTask: Promise<void> | null = null;

const isAuthRequest = (url: string) => url.includes('/auth/login') || url.includes('/auth/logout') || url.includes('/auth/register');

const clearSession = () => {
  wx.removeStorageSync('XIANGYUN_TOKEN');
  wx.removeStorageSync('XIANGYUN_USER');
  wx.removeStorageSync('XIANGYUN_ROLE');
};

const goLogin = () => {
  wx.reLaunch({ url: '/pages/login/index' });
};

const toRequestError = (error: unknown, fallback: string, statusCode?: number): RequestError => {
  const body = error as Partial<ApiResponse<unknown>> | undefined;
  if (body && typeof body === 'object' && typeof body.message === 'string' && body.message) {
    return { message: body.message, statusCode, raw: error };
  }
  if (error instanceof Error) {
    return { message: error.message || fallback, statusCode, raw: error };
  }
  return { message: fallback, statusCode, raw: error };
};

const parseResponse = <T>(raw: unknown): T => {
  if (typeof raw === 'string') {
    const text = raw.trim();
    if (!text) {
      return undefined as T;
    }
    try {
      return parseResponse<T>(JSON.parse(text));
    } catch (error) {
      throw { message: '响应数据格式错误', raw: error };
    }
  }
  const response = raw as ApiResponse<T>;
  if (response && typeof response === 'object' && 'data' in response) {
    if (response.code && response.code !== 200 && response.code !== 0) {
      throw toRequestError(response, response.message || '请求失败', response.code);
    }
    return response.data as T;
  }
  return raw as T;
};

const ensureDemoLogin = (url: string): Promise<void> => {
  if (!envConfig.demoAutoLogin || envConfig.dataSource !== 'api' || isAuthRequest(url)) {
    return Promise.resolve();
  }
  if (wx.getStorageSync('XIANGYUN_TOKEN')) {
    return Promise.resolve();
  }
  if (demoLoginTask) {
    return demoLoginTask;
  }

  demoLoginTask = new Promise<void>((resolve, reject) => {
    wx.request({
      url: `${envConfig.baseURL}/auth/login`,
      method: 'POST',
      data: envConfig.demoAccount,
      timeout: envConfig.timeout,
      header: { 'content-type': 'application/json' },
      success(result) {
        try {
          const statusCode = result.statusCode || 0;
          if (statusCode < 200 || statusCode >= 300) {
            reject(toRequestError(result.data, `HTTP ${statusCode}`, statusCode));
            return;
          }
          const data = parseResponse<{ token: string; user: { role: string } }>(result.data);
          wx.setStorageSync('XIANGYUN_TOKEN', data.token);
          wx.setStorageSync('XIANGYUN_USER', data.user);
          wx.setStorageSync('XIANGYUN_ROLE', data.user.role);
          resolve();
        } catch (error) {
          reject(toRequestError(error, '自动登录失败'));
        }
      },
      fail(error) {
        reject(toRequestError(error, error.errMsg || '自动登录失败'));
      }
    });
  }).finally(() => {
    demoLoginTask = null;
  });

  return demoLoginTask;
};

export const request = <T = unknown, D = unknown>(options: RequestOptions<D>): Promise<T> => {
  const { url, method = 'GET', data, header = {}, timeout = envConfig.timeout } = options;

  const send = (): Promise<T> => new Promise((resolve, reject) => {
    wx.request({
      url: buildUrl(url),
      method,
      data,
      timeout,
      header: {
        'content-type': 'application/json',
        ...getAuthHeader(),
        ...header
      },
      success(result) {
        try {
          const statusCode = result.statusCode || 0;
          if (statusCode === 401 && !isAuthRequest(url)) {
            clearSession();
            goLogin();
            reject(toRequestError(result.data, '登录已过期，请重新登录', statusCode));
            return;
          }
          if (statusCode < 200 || statusCode >= 300) {
            throw toRequestError(result.data, `HTTP ${statusCode}`, statusCode);
          }
          resolve(parseResponse<T>(result.data));
        } catch (error) {
          reject(toRequestError(error, '请求失败'));
        }
      },
      fail(error) {
        reject(toRequestError(error, error.errMsg || '网络请求异常'));
      }
    });
  });

  return ensureDemoLogin(url).then(send);
};

export const get = <T = unknown>(url: string, data?: unknown) => request<T>({ url, method: 'GET', data });
export const post = <T = unknown>(url: string, data?: unknown) => request<T>({ url, method: 'POST', data });
export const put = <T = unknown>(url: string, data?: unknown) => request<T>({ url, method: 'PUT', data });
export const del = <T = unknown>(url: string, data?: unknown) => request<T>({ url, method: 'DELETE', data });

export const http = {
  get,
  post,
  put,
  delete: del
};
