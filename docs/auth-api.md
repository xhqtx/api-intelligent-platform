# 认证服务接口文档

> 网关基础地址：http://localhost:8080

## 目录
- [用户认证模块](#用户认证模块)
- [权限组管理模块](#权限组管理模块)
- [权限管理模块](#权限管理模块)
- [通用说明](#通用说明)

## 用户认证模块

### 1. 用户注册
- **接口**：`POST /auth/api/auth/register`
- **描述**：新用户注册
- **请求体**：
```json
{
    "username": "string",
    "password": "string",
    "email": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "username": "string",
        "email": "string"
    }
}
```

### 2. 用户登录
- **接口**：`POST /auth/api/auth/login`
- **描述**：用户登录获取令牌
- **请求体**：
```json
{
    "username": "string",
    "password": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "accessToken": "string",
        "refreshToken": "string",
        "tokenType": "string"
    }
}
```

### 3. 用户登出
- **接口**：`POST /auth/api/auth/logout`
- **描述**：用户退出登录
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

### 4. 获取当前用户信息
- **接口**：`GET /auth/api/auth/me`
- **描述**：获取当前登录用户的详细信息
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "username": "string",
        "email": "string",
        "status": "boolean",
        "roles": [
            {
                "id": "number",
                "name": "string"
            }
        ]
    }
}
```

### 5. 刷新令牌
- **接口**：`POST /auth/api/auth/refresh`
- **描述**：使用刷新令牌获取新的访问令牌
- **请求体**：
```json
{
    "refreshToken": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "accessToken": "string",
        "refreshToken": "string",
        "tokenType": "string"
    }
}
```

### 6. 获取所有用户
- **接口**：`GET /auth/api/auth/users`
- **描述**：分页获取所有用户列表
- **权限要求**：管理员权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求参数**：
  - `page`: 页码，默认值为1
  - `size`: 每页大小，默认值为10
  - `sortBy`: 排序字段，可选
  - `sortDirection`: 排序方向，可选
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "content": [
            {
                "id": "number",
                "username": "string",
                "email": "string",
                "enabled": "boolean",
                "roles": []
            }
        ],
        "pageable": {
            "sort": {
                "sorted": "boolean",
                "unsorted": "boolean",
                "empty": "boolean"
            },
            "pageNumber": "number",
            "pageSize": "number",
            "offset": "number",
            "paged": "boolean",
            "unpaged": "boolean"
        },
        "totalElements": "number",
        "totalPages": "number",
        "last": "boolean",
        "first": "boolean",
        "size": "number",
        "number": "number",
        "sort": {
            "sorted": "boolean",
            "unsorted": "boolean",
            "empty": "boolean"
        },
        "numberOfElements": "number",
        "empty": "boolean"
    }
}
```

## 权限组管理模块

### 1. 创建权限组
- **接口**：`POST /auth/api/permission-groups`
- **描述**：创建新的权限组
- **权限要求**：`PERMISSION_GROUP_MANAGE`
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
- **权限要求**：`PERMISSION_GROUP_MANAGE`
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
- **权限要求**：`PERMISSION_GROUP_MANAGE`
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
- **描述**：获取系统中所有权限组列表
- **权限要求**：`PERMISSION_GROUP_MANAGE`
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
- **权限要求**：`PERMISSION_GROUP_MANAGE`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：与创建权限组响应格式相同

### 6. 添加权限到权限组
- **接口**：`POST /auth/api/permission-groups/{id}/permissions`
- **描述**：向指定权限组添加权限
- **权限要求**：`PERMISSION_GROUP_MANAGE`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"]  // 权限ID列表
```
- **响应**：与创建权限组响应格式相同

### 7. 从权限组移除权限
- **接口**：`DELETE /auth/api/permission-groups/{id}/permissions`
- **描述**：从指定权限组移除权限
- **权限要求**：`PERMISSION_GROUP_MANAGE`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"]  // 权限ID列表
```
- **响应**：与创建权限组响应格式相同

## 权限管理模块

### 1. 创建权限
- **接口**：`POST /auth/api/permissions`
- **描述**：创建新的权限
- **权限要求**：`PERMISSION_MANAGE`
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
- **描述**：更新现有权限信息
- **权限要求**：`PERMISSION_MANAGE`
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
- **权限要求**：`PERMISSION_MANAGE`
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
- **描述**：获取系统中所有权限列表
- **权限要求**：`PERMISSION_MANAGE`
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
- **权限要求**：`PERMISSION_MANAGE`
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
- 409：资源冲突（如注册时用户名已存在）
- 500：服务器内部错误

例如，登录失败的响应：
```json
{
    "code": "AUTH_FAILED",
    "message": "Invalid username or password",
    "data": null
}
```