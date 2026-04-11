import type { PageLoadState } from '../types/common';

export const PageState: Record<'Loading' | 'Ready' | 'Empty' | 'Error', PageLoadState> = {
  Loading: 'loading',
  Ready: 'ready',
  Empty: 'empty',
  Error: 'error'
};

export const DEFAULT_ERROR_MESSAGE = '\u6570\u636e\u52a0\u8f7d\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5';
export const DEFAULT_LOADING_TEXT = '\u6b63\u5728\u52a0\u8f7d\u4e61\u8018OS\u6570\u636e...';

export const getErrorMessage = (error: unknown, fallback = DEFAULT_ERROR_MESSAGE): string => {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  if (typeof error === 'string') {
    return error;
  }
  if (error && typeof error === 'object' && 'message' in error) {
    const message = (error as { message?: unknown }).message;
    if (typeof message === 'string' && message) {
      return message;
    }
  }
  return fallback;
};
