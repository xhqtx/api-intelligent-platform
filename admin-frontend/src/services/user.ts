import api from './api';

export interface Permission {
  id: number;
  name: string;
  description: string;
}

export interface RoleDTO {
  id: number;
  name: string;
  description: string;
  permissions: Permission[];
}

export interface UserDTO {
  id: number;
  username: string;
  email: string;
  enabled: boolean;  // 注意这里是 enabled 而不是 status
  roles: RoleDTO[];
  createdAt: string;
  updatedAt: string;
}

export interface PageableDTO {
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  offset: number;
  pageNumber: number;
  pageSize: number;
  unpaged: boolean;
  paged: boolean;
}

export interface PageDTO<T> {
  content: T[];
  pageable: PageableDTO;
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface UserRegistrationRequest {
  username: string;
  password: string;
  email: string;
  roles: number[]; // 角色ID列表
}

export interface UserUpdateRequest {
  email: string;
  status: boolean;
  roles: number[]; // 角色ID列表
}

// 获取用户列表
export const getUsers = async (page = 0, size = 10, username?: string, email?: string, enabled?: boolean, sort?: string): Promise<ApiResponse<PageDTO<UserDTO>>> => {
  const response = await api.get('/auth/api/users', {
    params: { page, size, username, email, enabled, sort }
  });
  return response.data;
};

// 获取单个用户
export const getUserById = async (id: number) => {
  const response = await api.get(`/auth/api/users/${id}`);
  return response.data;
};

// 创建新用户
export const createUser = async (data: UserRegistrationRequest) => {
  const response = await api.post('/auth/api/users', data);
  return response.data;
};

// 更新用户信息
export const updateUser = async (id: number, data: UserUpdateRequest) => {
  const response = await api.put(`/auth/api/users/${id}`, data);
  return response.data;
};

// 修改密码
export const changePassword = async (id: number, oldPassword: string, newPassword: string) => {
  const response = await api.put(`/auth/api/users/${id}/password`, { oldPassword, newPassword });
  return response.data;
};

// 删除用户
export const deleteUser = async (id: number) => {
  const response = await api.delete(`/auth/api/users/${id}`);
  return response.data;
};

// 获取用户角色
export const getUserRoles = async (id: number) => {
  const response = await api.get(`/auth/api/users/${id}/roles`);
  return response.data;
};

// 批量分配角色给用户
export const assignRolesToUser = async (id: number, roleIds: number[]) => {
  const response = await api.post(`/auth/api/users/${id}/roles`, roleIds);
  return response.data;
};

// 批量移除用户的角色
export const removeRolesFromUser = async (id: number, roleIds: number[]) => {
  const response = await api.delete(`/auth/api/users/${id}/roles`, { data: roleIds });
  return response.data;
};