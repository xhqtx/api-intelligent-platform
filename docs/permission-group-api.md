# 权限组管理接口文档

> 网关基础地址：http://localhost:8080

## 目录
- [权限组管理模块](#权限组管理模块)
- [通用说明](#通用说明)

## 权限组管理模块

### 1. 创建权限组
- **接口**：`POST /auth/api/permission-groups`
- **描述**：创建新的权限组
- **权限要求**：`permission:group:manage` 或 `role:manage`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "name": "string",
    "description": "string",
    "permissionIds": ["number"]
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "name": "string",
        "description": "string",
        "permissions": [
            {
                "id": "number",
                "name": "string",
                "description": "string"
            }
        ],
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 2. 更新权限组
- **接口**：`PUT /auth/api/permission-groups/{id}`
- **描述**：更新现有权限组信息
- **权限要求**：`permission:group:manage`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "name": "string",
    "description": "string",
    "permissionIds": ["number"]
}
```
- **响应**：与创建权限组响应格式相同

### 3. 删除权限组
- **接口**：`DELETE /auth/api/permission-groups/{id}`
- **描述**：删除指定权限组
- **权限要求**：`permission:group:manage`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": null
}
```

### 4. 获取所有权限组
- **接口**：`GET /auth/api/permission-groups`
- **描述**：获取系统中所有权限组列表（分页）
- **权限要求**：`permission:group:manage`
- **请求参数**：
  - `name`: 权限组名称（可选，用于模糊搜索）
  - `page`: 页码（默认为0）
  - `size`: 每页大小（默认为10）
  - `sort`: 排序字段（可选）
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": [
        {
            "id": "number",
            "name": "string",
            "description": "string",
            "permissions": [
                {
                    "id": "number",
                    "name": "string",
                    "description": "string"
                }
            ],
            "createdAt": "string",
            "updatedAt": "string"
        }
    ]
}
```

### 5. 获取指定权限组
- **接口**：`GET /auth/api/permission-groups/{id}`
- **描述**：获取特定权限组的详细信息
- **权限要求**：`permission:group:manage`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：与创建权限组响应格式相同

### 6. 获取权限组的权限
- **接口**：`GET /auth/api/permission-groups/{id}/permissions`
- **描述**：获取指定权限组的所有权限ID
- **权限要求**：`permission:group:manage`
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": ["number"]  // 权限ID列表
}
```

### 7. 更新权限组的权限
- **接口**：`PUT /auth/api/permission-groups/{id}/permissions`
- **描述**：更新指定权限组的权限（替换原有的所有权限）
- **权限要求**：`permission:group:manage`
- **请求体**：
```json
["number"]  // 权限ID列表
```
- **响应**：与创建权限组响应格式相同

### 8. 添加权限到权限组
- **接口**：`POST /auth/api/permission-groups/{id}/permissions`
- **描述**：向指定权限组添加权限
- **权限要求**：`permission:group:manage`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"]  // 权限ID列表
```
- **响应**：与创建权限组响应格式相同

### 9. 从权限组移除权限
- **接口**：`DELETE /auth/api/permission-groups/{id}/permissions`
- **描述**：从指定权限组移除权限
- **权限要求**：`permission:group:manage`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"]  // 权限ID列表
```
- **响应**：与创建权限组响应格式相同

## 通用说明

### 错误响应
所有接口的错误响应格式：
```json
{
    "code": "string",
    "message": "string",
    "data": null
}
```

### 常见错误码
- 400：请求参数错误
- 401：未授权
- 403：权限不足
- 404：资源不存在
- 409：资源冲突（如创建权限组时名称已存在）
- 500：服务器内部错误

例如，创建权限组失败的响应：
```json
{
    "code": "PERMISSION_GROUP_CREATE_FAILED",
    "message": "Permission group name already exists",
    "data": null
}
```