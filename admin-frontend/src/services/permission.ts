import api from './api';

// 权限相关API
export const permissionApi = {
  // 获取权限列表（支持分页）
  getAllPermissions: (params?: { page?: number; size?: number; sort?: string }) =>
    api.get('/auth/api/permissions', { params }),

  // 创建新权限
  createPermission: (data: { name: string; description: string }) =>
    api.post('/auth/api/permissions', data),

  // 更新权限
  updatePermission: (id: number, data: { name: string; description: string }) =>
    api.put(`/auth/api/permissions/${id}`, data).then(response => {
      if (response.data.success === false) {
        throw new Error(response.data.message || '更新权限失败');
      }
      return response;
    }),

  // 删除权限
  deletePermission: (id: number) => api.delete(`/auth/api/permissions/${id}`),

  // 获取指定权限
  getPermission: (id: number) => api.get(`/auth/api/permissions/${id}`),

  // 一键授权（扫描并更新权限）
  scanAndUpdatePermissions: () => api.post('/auth/api/admin/scan-permissions'),
};

// 权限组相关API
export const permissionGroupApi = {
  // 获取所有权限组（支持分页和搜索）
  getAllPermissionGroups: (params?: { page?: number; size?: number; sort?: string; name?: string }) => 
    api.get('/auth/api/permission-groups', { params }),

  // 创建新权限组
  createPermissionGroup: (data: { name: string; description: string; permissionIds?: number[] }) =>
    api.post('/auth/api/permission-groups', data),

  // 更新权限组
  updatePermissionGroup: (id: number, data: { name: string; description: string; permissionIds?: number[] }) =>
    api.put(`/auth/api/permission-groups/${id}`, data),

  // 删除权限组
  deletePermissionGroup: (id: number) => api.delete(`/auth/api/permission-groups/${id}`),

  // 获取指定权限组
  getPermissionGroup: (id: number) => api.get(`/auth/api/permission-groups/${id}`),

  // 获取权限组的权限
  getPermissionGroupPermissions: (groupId: number) => 
    api.get(`/auth/api/permission-groups/${groupId}/permissions`),

  // 更新权限组的权限
  updatePermissionGroupPermissions: (groupId: number, permissionIds: number[]) =>
    api.put(`/auth/api/permission-groups/${groupId}/permissions`, permissionIds),

  // 向权限组添加权限
  addPermissionsToGroup: (groupId: number, permissionIds: number[]) =>
    api.post(`/auth/api/permission-groups/${groupId}/permissions`, permissionIds),

  // 从权限组移除权限
  removePermissionsFromGroup: (groupId: number, permissionIds: number[]) =>
    api.delete(`/auth/api/permission-groups/${groupId}/permissions`, { data: permissionIds }),
};

// 权限变更日志相关API
export const permissionLogApi = {
  // 获取权限变更日志
  getPermissionLogs: (params: { entityType?: string; entityId?: number; page?: number; size?: number }) =>
    api.get('/auth/api/permission-logs', { params }),
    
  // 获取特定权限变更日志
  getPermissionLog: (id: number) => api.get(`/auth/api/permission-logs/${id}`),
};

export default {
  permissionApi,
  permissionGroupApi,
  permissionLogApi,
};