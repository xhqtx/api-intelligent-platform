# 权限管理接口文档

> 网关基础地址：http://localhost:8080

## 目录
- [权限管理模块](#权限管理模块)
- [通用说明](#通用说明)

## 权限管理模块

### 1. 创建权限
- **接口**：`POST /auth/api/permissions`
- **描述**：创建新的权限
- **权限要求**：`permission:create`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "name": "string",
    "description": "string"
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
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 2. 更新权限
- **接口**：`PUT /auth/api/permissions/{id}`
- **描述**：更新现有权限信息（注意：权限名称不允许修改）
- **权限要求**：`permission:update`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "name": "string",
    "description": "string"
}
```
- **响应**：与创建权限响应格式相同

### 3. 删除权限
- **接口**：`DELETE /auth/api/permissions/{id}`
- **描述**：删除指定权限
- **权限要求**：`permission:delete`
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

### 4. 获取所有权限
- **接口**：`GET /auth/api/permissions`
- **描述**：获取系统中所有权限列表（分页）
- **权限要求**：`permission:read`
- **请求参数**：
  - `page`: 页码，默认值为1
  - `size`: 每页大小，默认值为10
  - `sortBy`: 排序字段（可选）
  - `sortDirection`: 排序方向（可选）
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
            "createdAt": "string",
            "updatedAt": "string"
        }
    ]
}
```

### 5. 获取指定权限
- **接口**：`GET /auth/api/permissions/{id}`
- **描述**：获取特定权限的详细信息
- **权限要求**：`permission:read`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：与创建权限响应格式相同

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
- 409：资源冲突（如创建权限时名称已存在）
- 500：服务器内部错误

例如，创建权限失败的响应：
```json
{
    "code": "PERMISSION_CREATE_FAILED",
    "message": "Permission name already exists",
    "data": null
}
```