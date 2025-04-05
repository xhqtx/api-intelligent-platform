-- Create permissions table if not exists
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Create role_permissions table if not exists
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Insert basic permissions
INSERT INTO permissions (name, description, created_by, updated_by) VALUES
-- User management permissions
('user:manage', '用户管理权限', 'system', 'system'),
('user:create', '创建用户权限', 'system', 'system'),
('user:read', '查看用户权限', 'system', 'system'),
('user:update', '更新用户权限', 'system', 'system'),
('user:delete', '删除用户权限', 'system', 'system'),

-- Role management permissions
('role:manage', '角色管理权限', 'system', 'system'),
('role:create', '创建角色权限', 'system', 'system'),
('role:read', '查看角色权限', 'system', 'system'),
('role:update', '更新角色权限', 'system', 'system'),
('role:delete', '删除角色权限', 'system', 'system'),

-- Permission management permissions
('permission:group:manage', '权限组管理权限', 'system', 'system'),
('permission:group:create', '创建权限项组权限', 'system', 'system'),
('permission:group:read', '查看权限组项权限', 'system', 'system'),
('permission:group:update', '更新权限项组权限', 'system', 'system'),
('permission:group:delete', '删除权限组项权限', 'system', 'system'),

-- Dict management permissions
('dict:view', '查看字典权限', 'system', 'system'),
('dict:create', '创建字典权限', 'system', 'system'),
('dict:read', '查看字典权限', 'system', 'system'),
('dict:update', '更新字典权限', 'system', 'system'),
('dict:delete', '删除字典权限', 'system', 'system'),

-- System management permissions
('system:config', '系统配置权限', 'system', 'system'),
('system:monitor', '系统监控权限', 'system', 'system'),
('system:log', '系统日志权限', 'system', 'system'),
-- User management permissions

-- Role management permissions
('role:create', '创建角色权限', 'system', 'system'),
('role:read', '查看角色权限', 'system', 'system'),
('role:update', '更新角色权限', 'system', 'system'),
('role:delete', '删除角色权限', 'system', 'system'),

-- Permission management permissions
('permission:create', '创建权限项权限', 'system', 'system'),
('permission:read', '查看权限项权限', 'system', 'system'),
('permission:update', '更新权限项权限', 'system', 'system'),
('permission:delete', '删除权限项权限', 'system', 'system'),

-- Dict management permissions
('dict:create', '创建字典权限', 'system', 'system'),
('dict:read', '查看字典权限', 'system', 'system'),
('dict:update', '更新字典权限', 'system', 'system'),
('dict:delete', '删除字典权限', 'system', 'system'),

-- System management permissions
('system:config', '系统配置权限', 'system', 'system'),
('system:monitor', '系统监控权限', 'system', 'system'),
('system:log', '系统日志权限', 'system', 'system');

-- Assign all permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = role_id;