package com.example.auth.repository;

import com.example.auth.model.DictData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictDataRepository extends JpaRepository<DictData, Long> {
    
    List<DictData> findByDictTypeCodeOrderBySortAsc(String dictTypeCode);
    
    boolean existsByDictTypeCodeAndValue(String dictTypeCode, String value);
}