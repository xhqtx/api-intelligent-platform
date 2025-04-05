package com.example.auth.service;

import com.example.auth.controller.dto.DictDataRequest;
import com.example.auth.controller.dto.DictTypeRequest;
import com.example.auth.model.DictData;
import com.example.auth.model.DictType;
import com.example.auth.repository.DictDataRepository;
import com.example.auth.repository.DictTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class DictService {

    private final DictTypeRepository dictTypeRepository;
    private final DictDataRepository dictDataRepository;

    @Autowired
    public DictService(DictTypeRepository dictTypeRepository, DictDataRepository dictDataRepository) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictDataRepository = dictDataRepository;
    }

    // 字典类型相关方法
    public Page<DictType> getAllDictTypes(Pageable pageable) {
        return dictTypeRepository.findAll(pageable);
    }

    public DictType getDictTypeById(Long id) {
        return dictTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("字典类型不存在: " + id));
    }

    public DictType getDictTypeByCode(String code) {
        return dictTypeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("字典类型不存在: " + code));
    }

    @Transactional
    public DictType createDictType(DictTypeRequest request) {
        if (dictTypeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("字典类型编码已存在: " + request.getCode());
        }

        DictType dictType = new DictType();
        BeanUtils.copyProperties(request, dictType);
        return dictTypeRepository.save(dictType);
    }

    @Transactional
    public DictType updateDictType(Long id, DictTypeRequest request) {
        DictType dictType = getDictTypeById(id);
        
        // 如果修改了编码，需要检查新编码是否已存在
        if (!dictType.getCode().equals(request.getCode()) && 
                dictTypeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("字典类型编码已存在: " + request.getCode());
        }
        
        BeanUtils.copyProperties(request, dictType);
        return dictTypeRepository.save(dictType);
    }

    @Transactional
    public void deleteDictType(Long id) {
        dictTypeRepository.deleteById(id);
    }

    // 字典数据相关方法
    public List<DictData> getDictDataByTypeCode(String typeCode) {
        // 验证字典类型是否存在
        dictTypeRepository.findByCode(typeCode)
                .orElseThrow(() -> new EntityNotFoundException("字典类型不存在: " + typeCode));
        
        return dictDataRepository.findByDictTypeCodeOrderBySortAsc(typeCode);
    }

    public DictData getDictDataById(Long id) {
        return dictDataRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("字典数据不存在: " + id));
    }

    @Transactional
    public DictData createDictData(DictDataRequest request) {
        // 验证字典类型是否存在
        DictType dictType = getDictTypeByCode(request.getDictTypeCode());
        
        // 验证同一字典类型下的值是否唯一
        if (dictDataRepository.existsByDictTypeCodeAndValue(request.getDictTypeCode(), request.getValue())) {
            throw new IllegalArgumentException("同一字典类型下键值已存在: " + request.getValue());
        }
        
        DictData dictData = new DictData();
        BeanUtils.copyProperties(request, dictData);
        dictData.setDictTypeId(dictType.getId());
        
        return dictDataRepository.save(dictData);
    }

    @Transactional
    public DictData updateDictData(Long id, DictDataRequest request) {
        DictData dictData = getDictDataById(id);
        
        // 验证字典类型是否存在
        DictType dictType = getDictTypeByCode(request.getDictTypeCode());
        
        // 如果修改了字典类型或值，需要检查新的组合是否已存在
        if ((!dictData.getDictTypeCode().equals(request.getDictTypeCode()) || 
                !dictData.getValue().equals(request.getValue())) && 
                dictDataRepository.existsByDictTypeCodeAndValue(request.getDictTypeCode(), request.getValue())) {
            throw new IllegalArgumentException("同一字典类型下键值已存在: " + request.getValue());
        }
        
        BeanUtils.copyProperties(request, dictData);
        dictData.setDictTypeId(dictType.getId());
        
        return dictDataRepository.save(dictData);
    }

    @Transactional
    public void deleteDictData(Long id) {
        dictDataRepository.deleteById(id);
    }
}