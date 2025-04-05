import api from './api';
import store from '@/store';

// 接口响应类型定义
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

// 用户注册请求体类型
interface RegisterRequest {
  username: string;
  password: string;
  email: string;
}

// 用户注册响应数据类型
interface RegisterResponse {
  id: number;
  username: string;
  email: string;
}

// 登录请求体类型
interface LoginRequest {
  username: string;
  password: string;
}

// 登录响应数据类型
interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

// 用户角色类型（用于类型检查）
export type Role = {
  id: number;
  name: string;
};

// 认证服务类
const AuthService = {
  /**
   * 检查用户是否已认证
   * @returns {boolean} 是否已认证
   */
  isAuthenticated(): boolean {
    return !!localStorage.getItem('admin_token');
  },

  /**
   * 检查用户是否拥有指定角色
   * @param roleName 角色名称
   * @returns {boolean} 是否拥有该角色
   */
  hasRole(roleName: string): boolean {
    const user = store.state.user;
    if (!user || !user.roles) return false;
    return user.roles.some(role => role.name === roleName);
  },

  /**
   * 用户注册
   * @param data 注册信息
   */
  async register(data: RegisterRequest): Promise<ApiResponse<RegisterResponse>> {
    const response = await api.post<ApiResponse<RegisterResponse>>('/auth/api/auth/register', data);
    return response.data;
  },

  /**
   * 用户登录
   * @param data 登录信息
   */
  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    const response = await api.post<ApiResponse<LoginResponse>>('/auth/api/auth/login', data);
    if (response.data.success && response.data.data) {
      // 保存token到localStorage
      localStorage.setItem('admin_token', response.data.data.accessToken);
      localStorage.setItem('admin_refresh_token', response.data.data.refreshToken);
      
      // 获取用户信息并更新到store
      await store.dispatch('fetchCurrentUser');
      
      // 检查是否成功获取了用户信息
      const currentUser = store.state.user;
      if (!currentUser || !currentUser.roles || currentUser.roles.length === 0) {
        console.error('Failed to fetch user information after login');
        return {
          success: false,
          message: 'Failed to fetch user information',
          data: {} as LoginResponse,
          timestamp: new Date().toISOString()
        };
      }
    }
    return response.data;
  },

  /**
   * 用户登出
   */
  async logout(): Promise<ApiResponse<null>> {
    const response = await api.post<ApiResponse<null>>('/auth/api/auth/logout');
    if (response.data.success) {
      // 清除本地存储的token
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_refresh_token');
    }
    return response.data;
  },

  /**
   * 刷新令牌
   */
  async refreshToken(): Promise<ApiResponse<LoginResponse>> {
    const refreshToken = localStorage.getItem('admin_refresh_token');
    if (!refreshToken) {
      throw new Error('No refresh token found');
    }

    const response = await api.post<ApiResponse<LoginResponse>>('/auth/api/auth/refresh', {
      refreshToken
    });

    if (response.data.success && response.data.data) {
      // 更新本地存储的token
      localStorage.setItem('admin_token', response.data.data.accessToken);
      localStorage.setItem('admin_refresh_token', response.data.data.refreshToken);
    }
    return response.data;
  },


  /**
   * 获取当前token
   */
  getToken(): string | null {
    return localStorage.getItem('admin_token');
  },

  /**
   * 获取当前refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('admin_refresh_token');
  },

};

export default AuthService;
export { AuthService };