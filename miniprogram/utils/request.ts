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
  // Token can be wired here when real login is enabled.
  return {};
};

const toRequestError = (error: unknown, fallback: string, statusCode?: number): RequestError => {
  if (error instanceof Error) {
    return { message: error.message || fallback, statusCode, raw: error };
  }
  return { message: fallback, statusCode, raw: error };
};

const parseResponse = <T>(raw: unknown): T => {
  const response = raw as ApiResponse<T>;
  if (response && typeof response === 'object' && 'data' in response) {
    if (response.code && response.code !== 200 && response.code !== 0) {
      throw toRequestError(response, response.message || '\u8bf7\u6c42\u5931\u8d25', response.code);
    }
    return response.data as T;
  }
  return raw as T;
};

export const request = <T = unknown, D = unknown>(options: RequestOptions<D>): Promise<T> => {
  const { url, method = 'GET', data, header = {}, timeout = envConfig.timeout } = options;

  return new Promise((resolve, reject) => {
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
          if (statusCode < 200 || statusCode >= 300) {
            throw toRequestError(result.data, `HTTP ${statusCode}`, statusCode);
          }
          resolve(parseResponse<T>(result.data));
        } catch (error) {
          reject(toRequestError(error, '\u8bf7\u6c42\u5931\u8d25'));
        }
      },
      fail(error) {
        reject(toRequestError(error, error.errMsg || '\u7f51\u7edc\u8bf7\u6c42\u5f02\u5e38'));
      }
    });
  });
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
