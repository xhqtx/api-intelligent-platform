package com.example.auth.service;

import com.example.auth.model.Permission;
import com.example.auth.model.PermissionGroup;
import com.example.auth.repository.PermissionRepository;
import com.example.auth.repository.PermissionGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionGroupRepository permissionGroupRepository;

    @Transactional(readOnly = true)
    public Permission findByName(String name) {
        return permissionRepository.findByName(name).orElse(null);
    }

    public PermissionService(PermissionRepository permissionRepository, PermissionGroupRepository permissionGroupRepository) {
        this.permissionRepository = permissionRepository;
        this.permissionGroupRepository = permissionGroupRepository;
    }

    @Transactional
    public PermissionGroup createPermissionGroup(String name, String description) {
        PermissionGroup group = new PermissionGroup(name, description);
        return permissionGroupRepository.save(group);
    }

    @Transactional
    public PermissionGroup updatePermissionGroup(Long id, String name, String description) {
        PermissionGroup group = permissionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission group not found"));
        group.setName(name);
        group.setDescription(description);
        return permissionGroupRepository.save(group);
    }

    @Transactional
    public void deletePermissionGroup(Long id) {
        permissionGroupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<PermissionGroup> findAllPermissionGroups(String name, Pageable pageable) {
        Specification<PermissionGroup> spec = (root, query, cb) -> {
            if (name != null && !name.isEmpty()) {
                return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
            }
            return null;
        };
        return permissionGroupRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<PermissionGroup> findPermissionGroupById(Long id) {
        return permissionGroupRepository.findById(id);
    }

    @Transactional
    public PermissionGroup addPermissionsToGroup(Long groupId, List<Long> permissionIds) {
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Permission group not found"));
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        group.getPermissions().addAll(permissions);
        return permissionGroupRepository.save(group);
    }

    @Transactional
    public PermissionGroup removePermissionsFromGroup(Long groupId, Set<Long> permissionIds) {
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Permission group not found"));
        group.getPermissions().removeIf(permission -> permissionIds.contains(permission.getId()));
        return permissionGroupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<Long> getPermissionGroupPermissions(Long groupId) {
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Permission group not found"));
        return group.getPermissions().stream().map(Permission::getId).collect(Collectors.toList());
    }

    @Transactional
    public PermissionGroup updatePermissionGroupPermissions(Long groupId, List<Long> permissionIds) {
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Permission group not found"));
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        group.setPermissions(permissions);
        return permissionGroupRepository.save(group);
    }

    @Transactional
    public Permission createPermission(String name, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Permission> listPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Permission> getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    @Transactional
    public Optional<Permission> updatePermission(Long id, String name, String description) {
        return permissionRepository.findById(id)
                .map(permission -> {
                    permission.setName(name);
                    permission.setDescription(description);
                    return permissionRepository.save(permission);
                });
    }

    @Transactional
    public boolean deletePermission(Long id) {
        if (permissionRepository.existsById(id)) {
            permissionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Set<Permission> getPermissionsByNames(Set<String> names) {
        return permissionRepository.findByNameIn(names);
    }

    @Transactional(readOnly = true)
    public Set<Permission> findAllById(Set<Long> ids) {
        return new HashSet<>(permissionRepository.findAllById(ids));
    }
}