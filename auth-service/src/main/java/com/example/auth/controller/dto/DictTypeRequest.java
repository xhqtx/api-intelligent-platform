package com.example.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class DictTypeRequest {
    
    @NotBlank(message = "字典类型编码不能为空")
    private String code;
    
    @NotBlank(message = "字典类型名称不能为空")
    private String name;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    private String remark;
}