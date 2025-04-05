package com.example.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class DictDataRequest {
    
    @NotBlank(message = "字典类型编码不能为空")
    private String dictTypeCode;
    
    @NotBlank(message = "字典标签不能为空")
    private String label;
    
    @NotBlank(message = "字典键值不能为空")
    private String value;
    
    private Integer sort;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    @NotNull(message = "是否默认不能为空")
    private Integer defaultFlag;
    
    private String remark;
}