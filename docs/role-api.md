# 角色管理接口文档

> 网关基础地址：http://localhost:8080

## 角色管理接口

### 1. 获取角色列表（分页）
- **接口**：`GET /auth/api/roles`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求参数**：
  - `name`：（可选）角色名称，支持模糊搜索
  - `status`：（可选）角色状态
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
                "name": "string",
                "description": "string",
                "status": "boolean",
                "permissions": [
                    {
                        "id": "number",
                        "name": "string",
                        "description": "string",
                        "code": "string"
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

### 2. 获取所有角色（不分页）
- **接口**：`GET /auth/api/roles/all`
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
            "status": "boolean",
            "permissions": [
                {
                    "id": "number",
                    "name": "string",
                    "description": "string",
                    "code": "string"
                }
            ],
            "createdAt": "string",
            "updatedAt": "string"
        }
    ]
}
```

### 3. 获取角色详情
- **接口**：`GET /auth/api/roles/{id}`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "name": "string",
        "description": "string",
        "status": "boolean",
        "permissions": [
            {
                "id": "number",
                "name": "string",
                "description": "string",
                "code": "string"
            }
        ],
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 4. 创建角色
- **接口**：`POST /auth/api/roles`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "name": "string",
    "description": "string",
    "permissions": ["number"] // 权限ID列表
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
        "status": "boolean",
        "permissions": [
            {
                "id": "number",
                "name": "string",
                "description": "string",
                "code": "string"
            }
        ],
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 5. 更新角色
- **接口**：`PUT /auth/api/roles/{id}`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "name": "string",
    "description": "string",
    "status": "boolean",
    "permissions": ["number"] // 权限ID列表
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
        "status": "boolean",
        "permissions": [
            {
                "id": "number",
                "name": "string",
                "description": "string",
                "code": "string"
            }
        ],
        "createdAt": "string",
        "updatedAt": "string"
    }
}
```

### 6. 删除角色
- **接口**：`DELETE /auth/api/roles/{id}`
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

### 7. 获取所有权限
- **接口**：`GET /auth/api/roles/permissions`
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
            "code": "string"
        }
    ]
}
```

### 8. 获取角色下的用户列表
- **接口**：`GET /auth/api/roles/{id}/users`
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
            "username": "string",
            "email": "string",
            "status": "boolean",
            "createdAt": "string",
            "updatedAt": "string"
        }
    ]
}
```

### 9. 批量分配用户到角色
- **接口**：`POST /auth/api/roles/{id}/users`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"] // 用户ID列表
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": null
}
```

### 10. 批量移除角色中的用户
- **接口**：`DELETE /auth/api/roles/{id}/users`
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
["number"] // 用户ID列表
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
所有接口都需要 `ADMIN` 或 `[ADMIN]` 权限。

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
- 409：资源冲突（如创建角色时名称已存在）
- 500：服务器内部错误