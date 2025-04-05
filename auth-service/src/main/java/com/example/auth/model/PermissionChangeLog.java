package com.example.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "permission_change_logs")
@NoArgsConstructor
public class PermissionChangeLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType action;

    public enum EntityType {
        ROLE,
        USER,
        GROUP
    }

    public enum ActionType {
        GRANT,
        REVOKE
    }

    public PermissionChangeLog(EntityType entityType, Long entityId, Permission permission, ActionType action) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.permission = permission;
        this.action = action;
    }
}