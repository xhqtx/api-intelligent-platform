package com.example.auth.repository;

import com.example.auth.model.PermissionChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionChangeLogRepository extends JpaRepository<PermissionChangeLog, Long> {
    List<PermissionChangeLog> findByEntityTypeAndEntityId(PermissionChangeLog.EntityType entityType, Long entityId);
}