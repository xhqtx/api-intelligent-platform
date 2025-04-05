package com.example.auth;

import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.RoleService;
import com.example.auth.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import com.example.common.security.JwtConfig;

@SpringBootApplication
@Import(JwtConfig.class)
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    public CommandLineRunner initRoles(RoleService roleService) {
        return args -> {
            // 如果角色不存在，则创建基本角色
            if (!roleService.existsByName("USER")) {
                roleService.createRole("USER", "Basic user role");
            }
            if (!roleService.existsByName("ADMIN")) {
                roleService.createRole("ADMIN", "Administrator role");
            }
        };
    }
    
    @Bean
    public CommandLineRunner initAdminUser(UserService userService, UserRepository userRepository) {
        return args -> {
            // 检查管理员用户是否存在
            if (userRepository.findByUsername("admin").isEmpty()) {
                // 创建管理员用户
                System.out.println("Creating admin user...");
                User adminUser = userService.registerUser("admin", "admin123", "admin@example.com");
                
                // 为管理员用户添加ADMIN角色
                userService.addRoleToUser("admin", "ADMIN");
                System.out.println("Admin user created successfully");
            } else {
                System.out.println("Admin user already exists");
            }
        };
    }
}