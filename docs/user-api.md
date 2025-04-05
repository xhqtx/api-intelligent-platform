# 用户管理接口文档

> 网关基础地址：http://localhost:8080

## 用户管理接口

### 1. 获取用户列表（分页）
- **接口**：`GET /auth/api/users`
- **权限**：需要 ADMIN 角色
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求参数**：
  - `username`：（可选）用户名，支持模糊搜索
  - `email`：（可选）邮箱，支持模糊搜索
  - `status`：（可选）用户状态
  - `page`：页码，从0开始
  - `size`：每页大小
  - `sort`：（可选）排序字段，如：`id,desc`
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
                "status": "boolean",
                "roles": [
                    {
                        "id": "number",
                        "name": "string"
                    }
                ],
                "createdAt": "string",
                "updatedAt": "string"
            }
        ],
        "totalElements": "number",
        "totalPages": "number",
        "size": "number",
        "number": "number"
    }
}
```

### 2. 获取指定用户信息
- **接口**：`GET /auth/api/users/{id}`
- **权限**：需要 ADMIN 角色或本人账号
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
        ],
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 3. 创建用户
- **接口**：`POST /auth/api/users`
- **权限**：需要 ADMIN 角色
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "username": "string",
    "email": "string",
    "password": "string",
    "roles": ["number"] // 角色ID列表
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
        "email": "string",
        "status": "boolean",
        "roles": [
            {
                "id": "number",
                "name": "string"
            }
        ],
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 4. 更新用户信息
- **接口**：`PUT /auth/api/users/{id}`
- **权限**：需要 ADMIN 角色或本人账号
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "email": "string",
    "status": "boolean",
    "roles": ["number"] // 角色ID列表
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
        "email": "string",
        "status": "boolean",
        "roles": [
            {
                "id": "number",
                "name": "string"
            }
        ],
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 5. 修改用户密码
- **接口**：`PUT /auth/api/users/{id}/password`
- **权限**：仅本人账号
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "oldPassword": "string",
    "newPassword": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": null
}
```

### 6. 删除用户
- **接口**：`DELETE /auth/api/users/{id}`
- **权限**：需要 ADMIN 角色
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

### 7. 获取用户角色列表
- **接口**：`GET /auth/api/users/{id}/roles`
- **权限**：需要 ADMIN 角色或本人账号
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
            "name": "string"
        }
    ]
}
```

### 8. 批量分配角色给用户
- **接口**：`POST /auth/api/users/{id}/roles`
- **权限**：需要 ADMIN 角色
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"] // 角色ID列表
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": null
}
```

### 9. 批量移除用户的角色
- **接口**：`DELETE /auth/api/users/{id}/roles`
- **权限**：需要 ADMIN 角色
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"] // 角色ID列表
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": null
}
```

## 通用说明

### 权限要求
除非特别说明，所有接口都需要 `ADMIN` 角色或相应的权限。

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
- 409：资源冲突（如创建用户时用户名已存在）
- 500：服务器内部错误