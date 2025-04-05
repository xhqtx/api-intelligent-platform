# API智能平台 - 已实现功能文档

## 1. 系统架构

该项目采用微服务架构，主要包含以下服务：

1. 认证服务（auth-service）：负责用户认证、授权和令牌管理
2. 网关服务（gateway-service）：负责路由转发和请求限流
3. 通用安全模块（common-security）：提供JWT相关功能

## 2. 认证服务（auth-service）

### 2.1 主要功能

1. 用户注册
2. 用户登录
3. 用户登出
4. 令牌刷新
5. 用户角色管理

### 2.2 API 端点

#### 2.2.1 认证相关 (AuthController)
所有认证相关的API通过网关访问时需要添加前缀 `/auth`

- POST /api/auth/register：用户注册
  - 请求体：username, password, email
  - 响应：注册成功的用户信息

- POST /api/auth/login：用户登录
  - 请求体：username, password
  - 响应：访问令牌和刷新令牌

- POST /api/auth/logout：用户登出
  - 请求头：需要包含有效的访问令牌
  - 响应：登出成功确认

- POST /api/auth/refresh：刷新访问令牌
  - 请求体：refreshToken
  - 响应：新的访问令牌

#### 2.2.2 用户管理 (UserController)
所有用户管理API通过网关访问时需要添加前缀 `/auth`

- GET /api/users：获取所有用户列表
  - 权限要求：ADMIN角色
  - 响应：用户列表

- GET /api/users/{username}：获取指定用户信息
  - 权限要求：ADMIN角色或当前用户查询自己
  - 路径参数：username
  - 响应：用户详细信息

- GET /api/users/{username}/roles：获取用户角色
  - 权限要求：ADMIN角色或当前用户查询自己
  - 路径参数：username
  - 响应：角色列表

- POST /api/users/role：为用户添加角色
  - 权限要求：ADMIN角色
  - 请求体：username, roleName
  - 响应：操作成功确认

- DELETE /api/users/role：移除用户角色
  - 权限要求：ADMIN角色
  - 请求体：username, roleName
  - 响应：操作成功确认

#### 2.2.3 角色管理 (RoleController)
所有角色管理API通过网关访问时需要添加前缀 `/auth`

- GET /api/roles：获取所有角色
  - 权限要求：ADMIN角色
  - 响应：角色列表

- GET /api/roles/{id}：获取指定角色
  - 权限要求：ADMIN角色
  - 路径参数：id
  - 响应：角色详细信息

- POST /api/roles：创建新角色
  - 权限要求：ADMIN角色
  - 请求体：name, description
  - 响应：创建的角色信息

- PUT /api/roles/{id}：更新角色
  - 权限要求：ADMIN角色
  - 路径参数：id
  - 请求体：name, description
  - 响应：更新后的角色信息

- DELETE /api/roles/{id}：删除角色
  - 权限要求：ADMIN角色
  - 路径参数：id
  - 响应：删除成功确认

### 2.3 安全特性

1. 密码加密：使用PasswordEncoder进行密码加密
2. JWT（JSON Web Token）：用于生成访问令牌和刷新令牌
3. Redis缓存：用于存储刷新令牌和维护令牌黑名单

### 2.4 用户服务

- 用户注册：创建新用户并分配默认角色（USER）
- 用户查询：根据用户名查找用户
- 角色管理：为用户添加/移除角色
- 用户详情：实现UserDetailsService接口，用于Spring Security的用户认证

## 3. 网关服务（gateway-service）

### 3.1 主要功能

1. 请求路由：将请求转发到相应的微服务
2. 请求过滤：移除敏感的HTTP头
3. 速率限制：基于Redis的请求速率限制

### 3.2 路由配置

- /auth/** -> http://localhost:8081：将认证相关的请求路由到认证服务
  - 网关端点：http://localhost:8080/auth/api/auth/* -> 认证服务的 /api/auth/*
  - 网关端点：http://localhost:8080/auth/api/users/* -> 认证服务的 /api/users/*
  - 网关端点：http://localhost:8080/auth/api/roles/* -> 认证服务的 /api/roles/*

### 3.3 安全特性

1. JWT验证：验证请求中的JWT令牌
2. 请求限流：使用Redis实现请求速率限制，默认设置为每秒10个请求，突发容量为20个请求

## 4. 通用安全模块（common-security）

提供JWT令牌生成和验证的通用功能，被其他服务模块使用。

## 5. 数据持久化

- 使用MySQL数据库存储用户和角色信息
- 使用Spring Data JPA进行对象关系映射
- 数据库配置：
  - URL: jdbc:mysql://localhost:3306/auth_db
  - 驱动：com.mysql.cj.jdbc.Driver
  - Hibernate方言：org.hibernate.dialect.MySQL8Dialect

## 6. 缓存策略

使用Redis作为缓存存储：
- 主机：localhost
- 端口：6379
- 数据库：0
- 超时时间：60000ms

## 7. 日志记录

使用SLF4J和Logback进行日志记录，主要记录：
- 认证服务的调试信息
- Spring Security的调试信息
- 网关服务的调试信息

## 8. 跨域资源共享（CORS）

认证服务配置允许所有来源的跨域请求，最大年龄为3600秒。

## 9. 错误处理

实现了全局异常处理，对常见错误（如认证失败、注册失败等）提供友好的错误响应。

## 10. 安全配置

- 使用Spring Security进行安全配置
- 实现了自定义的JWT过滤器
- 配置了密码编码器

这个文档概述了API智能平台目前已实现的主要功能和特性。系统采用了微服务架构，实现了基本的用户认证、授权和API网关功能，并使用了多种安全措施来保护系统。如果您需要更详细的信息或有任何疑问，请随时告诉我。