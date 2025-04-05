import request from './request'
import { AxiosResponse } from 'axios'

export interface Role {
  id: number
  name: string
  description: string
  status: boolean
  permissions: Permission[]
  createdAt: string
  updatedAt: string
}

export interface Permission {
  id: number
  name: string
  description: string
  code: string
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface PermissionGroup {
  id: number
  name: string
  description: string
  permissions: Permission[]
  createdAt: string
  updatedAt: string
  createdBy: string
  updatedBy: string
}

export interface CreateRoleRequest {
  name: string
  description: string
  permissions: number[] // permission IDs
}

export interface UpdateRoleRequest extends CreateRoleRequest {
  status: boolean
}

export interface RoleQueryParams {
  page: number
  size: number
  name?: string
  status?: boolean
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

class RoleService {
  // 获取角色列表（分页）
  async getRoles(params: RoleQueryParams): Promise<PageResponse<Role>> {
    const response: AxiosResponse = await request.get('/auth/api/roles', { params })
    return response.data.data
  }

  // 获取所有角色（不分页，用于下拉选择）
  async getAllRoles(): Promise<Role[]> {
    const response: AxiosResponse = await request.get('/auth/api/roles/all')
    return response.data.data
  }

  // 获取单个角色详情
  async getRole(id: number): Promise<Role> {
    const response: AxiosResponse = await request.get(`/auth/api/roles/${id}`)
    return response.data.data
  }

  // 创建角色
  async createRole(role: CreateRoleRequest): Promise<Role> {
    const response: AxiosResponse = await request.post('/auth/api/roles', role)
    return response.data.data
  }

  // 更新角色
  async updateRole(id: number, role: UpdateRoleRequest): Promise<Role> {
    const response: AxiosResponse = await request.put(`/auth/api/roles/${id}`, role)
    return response.data.data
  }

  // 删除角色
  async deleteRole(id: number): Promise<void> {
    await request.delete(`/auth/api/roles/${id}`)
  }

  // 获取所有权限
  async getAllPermissions(): Promise<Permission[]> {
    const response: AxiosResponse = await request.get('/auth/api/permissions')
    return response.data.data
  }

  // 获取所有权限组
async getPermissionGroups(): Promise<PageResponse<PermissionGroup>> {
  const response: AxiosResponse = await request.get('/auth/api/permission-groups')
  return response.data.data
}

  // 获取角色的用户列表
  async getRoleUsers(roleId: number): Promise<{ id: number; username: string; email: string }[]> {
    const response: AxiosResponse = await request.get(`/auth/api/roles/${roleId}/users`)
    return response.data.data
  }

  // 为角色分配用户
  async assignUsers(roleId: number, userIds: number[]): Promise<void> {
    await request.post(`/auth/api/roles/${roleId}/users`, { userIds })
  }

  // 移除角色中的用户
  async removeUsers(roleId: number, userIds: number[]): Promise<void> {
    await request.delete(`/auth/api/roles/${roleId}/users`, { data: { userIds } })
  }

  // 获取角色的权限列表
  async getRolePermissions(roleId: number): Promise<any> {
    const response: AxiosResponse = await request.get(`/auth/api/roles/${roleId}/permissions`)
    // 检查响应结构
    console.log('getRolePermissions response:', JSON.stringify(response.data))
    // 直接返回原始响应，让调用方处理数据结构
    return response.data
  }
}

export default new RoleService()