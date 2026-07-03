import { get, post } from '../utils/request';

export interface LoginUser {
  id: string;
  username: string;
  displayName: string;
  role: 'USER' | 'STAFF' | 'ADMIN';
  villageId: string;
  permissions: string[];
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: LoginUser;
}

export const login = async (username: string, password: string) => {
  const result = await post<LoginResponse>('/auth/login', { username, password });
  wx.setStorageSync('XIANGYUN_TOKEN', result.token);
  wx.setStorageSync('XIANGYUN_USER', result.user);
  wx.setStorageSync('XIANGYUN_ROLE', result.user.role);
  return result;
};

export const register = async (username: string, password: string, displayName: string) => {
  const result = await post<LoginResponse>('/auth/register', { username, password, displayName });
  wx.setStorageSync('XIANGYUN_TOKEN', result.token);
  wx.setStorageSync('XIANGYUN_USER', result.user);
  wx.setStorageSync('XIANGYUN_ROLE', result.user.role);
  return result;
};

export const getCurrentUser = () => get<LoginUser>('/auth/me');

export const logout = async () => {
  await post('/auth/logout');
  wx.removeStorageSync('XIANGYUN_TOKEN');
  wx.removeStorageSync('XIANGYUN_USER');
  wx.removeStorageSync('XIANGYUN_ROLE');
};

export const hasRole = (...roles: LoginUser['role'][]) => {
  const role = wx.getStorageSync('XIANGYUN_ROLE');
  return roles.includes(role);
};
