# 字典管理接口文档

> 网关基础地址：http://localhost:8080

## 字典类型接口

### 1. 获取字典类型列表（分页）
- **接口**：`GET /auth/api/dict/types`
- **权限**：需要 `dict:view` 权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求参数**：
  - `code`：（可选）字典类型编码，支持模糊搜索
  - `name`：（可选）字典类型名称，支持模糊搜索
  - `status`：（可选）状态
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
                "code": "string",
                "name": "string",
                "status": "boolean",
                "remark": "string",
                "createdAt": "string",
                "updatedAt": "string",
                "createdBy": "string",
                "updatedBy": "string"
            }
        ],
        "totalElements": "number",
        "totalPages": "number",
        "size": "number",
        "number": "number"
    }
}
```

### 2. 获取字典类型详情
- **接口**：`GET /auth/api/dict/types/{id}`
- **权限**：需要 `dict:view` 权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "code": "string",
        "name": "string",
        "status": "boolean",
        "remark": "string",
        "createdAt": "string",
        "updatedAt": "string",
        "createdBy": "string",
        "updatedBy": "string"
    }
}
```

### 3. 创建字典类型
- **接口**：`POST /auth/api/dict/types`
- **权限**：需要 `dict:create` 权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "code": "string",
    "name": "string",
    "status": "boolean",
    "remark": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "code": "string",
        "name": "string",
        "status": "boolean",
        "remark": "string",
        "createdAt": "string",
        "updatedAt": "string",
        "createdBy": "string",
        "updatedBy": "string"
    }
}
```

### 4. 更新字典类型
- **接口**：`PUT /auth/api/dict/types/{id}`
- **权限**：需要 `dict:update` 权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "name": "string",
    "status": "boolean",
    "remark": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "code": "string",
        "name": "string",
        "status": "boolean",
        "remark": "string",
        "createdAt": "string",
        "updatedAt": "string",
        "createdBy": "string",
        "updatedBy": "string"
    }
}
```

### 5. 删除字典类型
- **接口**：`DELETE /auth/api/dict/types/{id}`
- **权限**：需要 `dict:delete` 权限
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

## 字典数据接口

### 1. 获取指定类型的字典数据列表
- **接口**：`GET /auth/api/dict/data/{typeCode}`
- **权限**：需要 `dict:view` 权限
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
            "dictTypeId": "number",
            "dictTypeCode": "string",
            "label": "string",
            "value": "string",
            "sort": "number",
            "status": "boolean",
            "defaultFlag": "boolean",
            "remark": "string",
            "createdAt": "string",
            "updatedAt": "string",
            "createdBy": "string",
            "updatedBy": "string"
        }
    ]
}
```

### 2. 获取字典数据详情
- **接口**：`GET /auth/api/dict/data/item/{id}`
- **权限**：需要 `dict:view` 权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "dictTypeId": "number",
        "dictTypeCode": "string",
        "label": "string",
        "value": "string",
        "sort": "number",
        "status": "boolean",
        "defaultFlag": "boolean",
        "remark": "string",
        "createdAt": "string",
        "updatedAt": "string",
        "createdBy": "string",
        "updatedBy": "string"
    }
}
```

### 3. 创建字典数据
- **接口**：`POST /auth/api/dict/data`
- **权限**：需要 `dict:create` 权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "dictTypeCode": "string",
    "label": "string",
    "value": "string",
    "sort": "number",
    "status": "boolean",
    "defaultFlag": "boolean",
    "remark": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "dictTypeId": "number",
        "dictTypeCode": "string",
        "label": "string",
        "value": "string",
        "sort": "number",
        "status": "boolean",
        "defaultFlag": "boolean",
        "remark": "string",
        "createdAt": "string",
        "updatedAt": "string",
        "createdBy": "string",
        "updatedBy": "string"
    }
}
```

### 4. 更新字典数据
- **接口**：`PUT /auth/api/dict/data/{id}`
- **权限**：需要 `dict:update` 权限
- **请求头**：
  - `Authorization: Bearer {accessToken}`
- **请求体**：
```json
{
    "label": "string",
    "value": "string",
    "sort": "number",
    "status": "boolean",
    "defaultFlag": "boolean",
    "remark": "string"
}
```
- **响应**：
```json
{
    "code": "string",
    "message": "string",
    "data": {
        "id": "number",
        "dictTypeId": "number",
        "dictTypeCode": "string",
        "label": "string",
        "value": "string",
        "sort": "number",
        "status": "boolean",
        "defaultFlag": "boolean",
        "remark": "string",
        "createdAt": "string",
        "updatedAt": "string",
        "createdBy": "string",
        "updatedBy": "string"
    }
}
```

### 5. 删除字典数据
- **接口**：`DELETE /auth/api/dict/data/{id}`
- **权限**：需要 `dict:delete` 权限
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

## 通用说明

### 权限要求
所有接口都需要相应的权限，如 `dict:view`、`dict:create`、`dict:update`、`dict:delete`。

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
- 409：资源冲突（如创建字典类型时编码已存在）
- 500：服务器内部错误