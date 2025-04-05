-- 创建数据库
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（0停用 1正常）',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入基础角色数据
INSERT INTO roles (name, description, created_by) VALUES
('ADMIN', '系统管理员，拥有所有权限', 'system'),
('USER', '普通用户，拥有基本权限', 'system'),
('GUEST', '访客，仅拥有只读权限', 'system');

-- 创建权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_permissions_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_role_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建权限组表
CREATE TABLE IF NOT EXISTS permission_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_permission_groups_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建权限组与权限关联表
CREATE TABLE IF NOT EXISTS permission_group_permissions (
    group_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    PRIMARY KEY (group_id, permission_id),
    CONSTRAINT fk_pg_permissions_group_id FOREIGN KEY (group_id) REFERENCES permission_groups (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pg_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建权限变更日志表
CREATE TABLE IF NOT EXISTS permission_change_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型：ROLE, USER, GROUP',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型：GRANT, REVOKE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255) COMMENT '操作人',
    updated_by VARCHAR(255),
    CONSTRAINT fk_pcl_permission_id FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入预置权限组
INSERT INTO permission_groups (name, description, created_by) VALUES
('系统管理', '系统管理相关权限', 'system'),
('用户管理', '用户管理相关权限', 'system'),
('角色管理', '角色管理相关权限', 'system'),
('权限管理', '权限管理相关权限', 'system'),
('字典管理', '字典管理相关权限', 'system'),
('日志管理', '日志管理相关权限', 'system');

-- 插入预置权限数据
INSERT INTO permissions (name, description, created_by) VALUES
-- 系统管理权限
('system:view', '查看系统信息', 'system'),
('system:config', '配置系统参数', 'system'),
('system:monitor', '监控系统状态', 'system'),

-- 用户管理权限
('user:manage', '用户管理', 'system'),
('user:view', '查看用户', 'system'),
('user:create', '添加用户', 'system'),
('user:update', '编辑用户', 'system'),
('user:delete', '删除用户', 'system'),
('user:reset-pwd', '重置用户密码', 'system'),

-- 角色管理权限
('role:manage', '角色管理', 'system'),
('role:view', '查看角色', 'system'),
('role:create', '添加角色', 'system'),
('role:update', '编辑角色', 'system'),
('role:delete', '删除角色', 'system'),

-- 权限管理权限
('permission:manage', '权限管理', 'system'),
('permission:read', '查看权限', 'system'),
('permission:create', '添加权限', 'system'),
('permission:update', '编辑权限', 'system'),
('permission:delete', '删除权限', 'system'),

-- 权限组管理权限
('permission-group:manage', '权限组管理', 'system'),
('permission-group:view', '查看权限组', 'system'),
('permission-group:create', '创建权限组', 'system'),
('permission-group:update', '更新权限组', 'system'),
('permission-group:delete', '删除权限组', 'system'),
('permission-group:assign', '分配权限组权限', 'system'),

-- 字典管理权限
('dict:manage', '字典管理', 'system'),
('dict:view', '查看字典', 'system'),
('dict:create', '添加字典', 'system'),
('dict:update', '编辑字典', 'system'),
('dict:delete', '删除字典', 'system'),

-- 日志管理权限
('log:manage', '日志管理', 'system'),
('log:view', '查看日志', 'system'),
('log:export', '导出日志', 'system'),
('log:delete', '删除日志', 'system');

-- 将权限关联到权限组
-- 系统管理权限组
INSERT INTO permission_group_permissions (group_id, permission_id)
SELECT g.id, p.id
FROM permission_groups g, permissions p
WHERE g.name = '系统管理' AND p.name IN ('system:view', 'system:config', 'system:monitor');

-- 用户管理权限组
INSERT INTO permission_group_permissions (group_id, permission_id)
SELECT g.id, p.id
FROM permission_groups g, permissions p
WHERE g.name = '用户管理' AND p.name IN ('user:view', 'user:create', 'user:update', 'user:delete', 'user:reset-pwd');

-- 角色管理权限组
INSERT INTO permission_group_permissions (group_id, permission_id)
SELECT g.id, p.id
FROM permission_groups g, permissions p
WHERE g.name = '角色管理' AND p.name IN ('role:view', 'role:create', 'role:update', 'role:delete', 'role:manage');

-- 权限管理权限组
INSERT INTO permission_group_permissions (group_id, permission_id)
SELECT g.id, p.id
FROM permission_groups g, permissions p
WHERE g.name = '权限管理' AND p.name IN ('permission:read', 'permission:create', 'permission:update', 'permission:delete', 'permission:group:manage');

-- 字典管理权限组
INSERT INTO permission_group_permissions (group_id, permission_id)
SELECT g.id, p.id
FROM permission_groups g, permissions p
WHERE g.name = '字典管理' AND p.name IN ('dict:view', 'dict:create', 'dict:update', 'dict:delete');

-- 日志管理权限组
INSERT INTO permission_group_permissions (group_id, permission_id)
SELECT g.id, p.id
FROM permission_groups g, permissions p
WHERE g.name = '日志管理' AND p.name IN ('log:view', 'log:export', 'log:delete');

-- 为ADMIN角色分配所有字典管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN ('dict:view', 'dict:create', 'dict:update', 'dict:delete');

-- 为USER角色分配查看字典权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER' AND p.name = 'dict:view';

-- 为ADMIN角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id 
    AND rp.permission_id = p.id
);

-- 创建初始管理员用户（密码需要使用BCrypt加密后再插入）
-- 这里的密码是BCrypt加密后的'admin123'
INSERT INTO users (username, password, email, enabled, created_by) VALUES
('admin', '$2a$10$EqKcp1WFKVQoYwRm2Pwch.vBDv.GQoCRWqhJ8.B1TQcX.EgIQgL2.', 'admin@example.com', 1, 'system');

-- 为管理员用户分配管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';

-- 创建字典类型表
CREATE TABLE IF NOT EXISTS dict_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    name VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    remark VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_dict_types_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 创建字典数据表
CREATE TABLE IF NOT EXISTS dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL COMMENT '字典类型ID',
    dict_type_code VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    label VARCHAR(100) NOT NULL COMMENT '字典标签',
    value VARCHAR(100) NOT NULL COMMENT '字典键值',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（0停用 1正常）',
    default_flag TINYINT DEFAULT 0 COMMENT '是否默认（0否 1是）',
    remark VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_dict_data_type_id FOREIGN KEY (dict_type_id) REFERENCES dict_types (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_dict_data_type_value UNIQUE (dict_type_code, value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 插入一些示例字典类型
INSERT INTO dict_types (code, name, remark, created_by) VALUES
('sys_user_sex', '用户性别', '用户性别列表', 'system'),
('sys_normal_disable', '系统开关', '系统开关列表', 'system'),
('sys_yes_no', '系统是否', '系统是否列表', 'system');

-- 插入一些示例字典数据
INSERT INTO dict_data (dict_type_id, dict_type_code, label, value, sort, status, default_flag, remark, created_by)
SELECT id, code, '男', '0', 1, 1, 1, '性别男', 'system' FROM dict_types WHERE code = 'sys_user_sex'
UNION ALL
SELECT id, code, '女', '1', 2, 1, 0, '性别女', 'system' FROM dict_types WHERE code = 'sys_user_sex'
UNION ALL
SELECT id, code, '未知', '2', 3, 1, 0, '性别未知', 'system' FROM dict_types WHERE code = 'sys_user_sex'
UNION ALL
SELECT id, code, '正常', '0', 1, 1, 1, '正常状态', 'system' FROM dict_types WHERE code = 'sys_normal_disable'
UNION ALL
SELECT id, code, '停用', '1', 2, 1, 0, '停用状态', 'system' FROM dict_types WHERE code = 'sys_normal_disable'
UNION ALL
SELECT id, code, '是', 'Y', 1, 1, 1, '系统默认是', 'system' FROM dict_types WHERE code = 'sys_yes_no'
UNION ALL
SELECT id, code, '否', 'N', 2, 1, 0, '系统默认否', 'system' FROM dict_types WHERE code = 'sys_yes_no';