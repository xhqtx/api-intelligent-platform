package com.example.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;



@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dict_data")
public class DictData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_type_id", nullable = false)
    private Long dictTypeId;

    @Column(name = "dict_type_code", nullable = false)
    private String dictTypeCode;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String value;

    private Integer sort;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "default_flag", nullable = false)
    private Integer defaultFlag;

    private String remark;
}