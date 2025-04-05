package com.example.auth.controller;

import com.example.auth.controller.dto.DictDataRequest;
import com.example.auth.controller.dto.DictTypeRequest;
import com.example.auth.model.DictData;
import com.example.auth.model.DictType;
import com.example.auth.service.DictService;
import com.example.common.security.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.auth.utils.PageUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final DictService dictService;

    @Autowired
    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    // 字典类型相关接口
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('dict:view')")
    public ApiResponse<Page<DictType>> getAllDictTypes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Pageable pageable = PageUtils.convertToPageable(page, size, sortBy, sortDirection);
        return ApiResponse.success(dictService.getAllDictTypes(pageable));
    }

    @GetMapping("/types/{id}")
    @PreAuthorize("hasAuthority('dict:view')")
    public ApiResponse<DictType> getDictTypeById(@PathVariable Long id) {
        return ApiResponse.success(dictService.getDictTypeById(id));
    }

    @PostMapping("/types")
    @PreAuthorize("hasAuthority('dict:create')")
    public ApiResponse<DictType> createDictType(@Valid @RequestBody DictTypeRequest request) {
        return ApiResponse.success(dictService.createDictType(request), HttpStatus.CREATED);
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasAuthority('dict:update')")
    public ApiResponse<DictType> updateDictType(@PathVariable Long id, @Valid @RequestBody DictTypeRequest request) {
        return ApiResponse.success(dictService.updateDictType(id, request));
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasAuthority('dict:delete')")
    public ApiResponse<Void> deleteDictType(@PathVariable Long id) {
        dictService.deleteDictType(id);
        return ApiResponse.success(null);
    }

    // 字典数据相关接口
    @GetMapping("/data/{typeCode}")
    @PreAuthorize("hasAuthority('dict:view')")
    public ApiResponse<List<DictData>> getDictDataByTypeCode(@PathVariable String typeCode) {
        return ApiResponse.success(dictService.getDictDataByTypeCode(typeCode));
    }

    @GetMapping("/data/item/{id}")
    @PreAuthorize("hasAuthority('dict:view')")
    public ApiResponse<DictData> getDictDataById(@PathVariable Long id) {
        return ApiResponse.success(dictService.getDictDataById(id));
    }

    @PostMapping("/data")
    @PreAuthorize("hasAuthority('dict:create')")
    public ApiResponse<DictData> createDictData(@Valid @RequestBody DictDataRequest request) {
        return ApiResponse.success(dictService.createDictData(request), HttpStatus.CREATED);
    }

    @PutMapping("/data/{id}")
    @PreAuthorize("hasAuthority('dict:update')")
    public ApiResponse<DictData> updateDictData(@PathVariable Long id, @Valid @RequestBody DictDataRequest request) {
        return ApiResponse.success(dictService.updateDictData(id, request));
    }

    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasAuthority('dict:delete')")
    public ApiResponse<Void> deleteDictData(@PathVariable Long id) {
        dictService.deleteDictData(id);
        return ApiResponse.success(null);
    }
}