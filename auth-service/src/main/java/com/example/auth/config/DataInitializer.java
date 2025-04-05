package com.example.auth.config;

import com.example.auth.model.Permission;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 创建角色（如果不存在）
            createRoleIfNotFound(roleRepository, "ADMIN");
            createRoleIfNotFound(roleRepository, "USER");

            // 创建管理员用户（如果不存在）
            if (userService.findByUsername("admin") == null) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123")); // 设置初始密码
                adminUser.setEmail("admin@example.com");
                adminUser.setEnabled(true);
                adminUser.setCreatedBy("system");
                adminUser.setUpdatedBy("system");

                Role adminRole = roleRepository.findByName("ADMIN").orElseThrow(() -> new RuntimeException("Role not found"));
                adminUser.addRole(adminRole);

                userService.registerUser(adminUser.getUsername(), adminUser.getPassword(), adminUser.getEmail());
                System.out.println("Admin user created successfully.");
            } else {
                System.out.println("Admin user already exists.");
            }
        };
    }

    private void createRoleIfNotFound(RoleRepository roleRepository, String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            if ("ADMIN".equals(name)) {
                // 为ADMIN角色添加所需权限
                role.addPermission(createPermission("role:manage"));
                role.addPermission(createPermission("dict:view"));
                role.addPermission(createPermission("dict:create"));
                role.addPermission(createPermission("dict:update"));
                role.addPermission(createPermission("dict:delete"));
                role.addPermission(createPermission("permission:group:manage"));
                role.addPermission(createPermission("permission:create"));
                role.addPermission(createPermission("permission:read"));
            }
            roleRepository.save(role);
            System.out.println("Role " + name + " created successfully.");
        }
    }

    private Permission createPermission(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription("Auto generated permission for " + name);
        return permission;
    }
}