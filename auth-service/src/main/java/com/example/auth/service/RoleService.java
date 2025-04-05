package com.example.auth.service;

import com.example.auth.model.Permission;
import com.example.auth.model.Role;
import com.example.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.example.auth.model.User;

@Service
public class RoleService {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionService permissionService;

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Role> findAll(String name, Boolean status, Pageable pageable) {
        Specification<Role> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return roleRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional
    public Role createRole(String name, String description) {
        Role role = new Role(name, description);
        String currentUser = getCurrentUsername();
        role.setCreatedBy(currentUser);
        role.setUpdatedBy(currentUser);
        return roleRepository.save(role);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }

    @Transactional
    public Role updateRole(Long id, String name, String description) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(name);
        role.setDescription(description);
        role.setUpdatedBy(getCurrentUsername());
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Transactional
    public void addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Permission permission = permissionService.getPermissionById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.addPermission(permission);
        role.setUpdatedBy(getCurrentUsername());
        roleRepository.save(role);
    }

    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Permission permission = permissionService.getPermissionById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.removePermission(permission);
        role.setUpdatedBy(getCurrentUsername());
        roleRepository.save(role);
    }

    @Transactional
    public Role createRole(String name, String description, Set<Long> permissionIds) {
        Role role = new Role(name, description);
        String currentUser = getCurrentUsername();
        role.setCreatedBy(currentUser);
        role.setUpdatedBy(currentUser);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            Set<Permission> permissions = permissionService.findAllById(permissionIds);
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(Long id, String name, String description, Set<Long> permissionIds, Boolean status) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setName(name);
        role.setDescription(description);
        if (status != null) {
            role.setStatus(status);
        }

        if (permissionIds != null) {
            Set<Permission> permissions = permissionService.findAllById(permissionIds);
            role.setPermissions(permissions);
        }

        role.setUpdatedBy(getCurrentUsername());
        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByRoleId(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return role.getUsers().stream().toList();
    }

    @Transactional
    public void assignUsersToRole(Long roleId, List<Long> userIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<User> users = userService.findAllById(userIds);
        users.forEach(user -> user.getRoles().add(role));
        role.getUsers().addAll(users);
        role.setUpdatedBy(getCurrentUsername());
        roleRepository.save(role);
    }

    @Transactional
    public void removeUsersFromRole(Long roleId, List<Long> userIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<User> users = userService.findAllById(userIds);
        users.forEach(user -> user.getRoles().remove(role));
        role.getUsers().removeAll(users);
        role.setUpdatedBy(getCurrentUsername());
        roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(Long id, String name, String description, Boolean status, Set<Long> permissionIds, Set<Long> userIds) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setName(name);
        role.setDescription(description);
        if (status != null) {
            role.setStatus(status);
        }

        // Update permissions
        if (permissionIds != null) {
            Set<Permission> permissions = new HashSet<>(permissionService.findAllById(permissionIds));
            role.getPermissions().clear();
            role.getPermissions().addAll(permissions);
        }

        // Update users
        if (userIds != null) {
            Set<User> users = new HashSet<>(userService.findAllById(userIds));
            role.getUsers().clear();
            role.getUsers().addAll(users);
        }

        role.setUpdatedBy(getCurrentUsername());
        return roleRepository.save(role);
    }
}