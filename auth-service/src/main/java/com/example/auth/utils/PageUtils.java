package com.example.auth.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

/**
 * Pagination utility class
 */
public class PageUtils {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MIN_PAGE = 1;
    private static final int MIN_SIZE = 1;

    /**
     * Create a pageable request object
     * 
     * @param page Page number (starting from 1)
     * @param size Page size
     * @return Pageable object
     */
    public static Pageable convertToPageable(Integer page, Integer size) {
        return convertToPageable(page, size, null);
    }

    /**
     * Create a pageable request object with sorting
     * 
     * @param page Page number (starting from 1)
     * @param size Page size
     * @param sort Sort object
     * @return Pageable object
     */
    public static Pageable convertToPageable(Integer page, Integer size, Sort sort) {
        // Parameter validation and default value handling
        int pageNumber = (page == null || page < MIN_PAGE) ? DEFAULT_PAGE : page;
        int pageSize = (size == null || size < MIN_SIZE) ? DEFAULT_SIZE : size;
        
        // Convert to zero-based page number
        int zeroBasedPage = pageNumber - 1;
        
        // Create pageable request
        return sort == null ? 
            PageRequest.of(zeroBasedPage, pageSize) : 
            PageRequest.of(zeroBasedPage, pageSize, sort);
    }

    /**
     * Create a pageable request object with sort field and direction
     * 
     * @param page Page number (starting from 1)
     * @param size Page size
     * @param sortBy Sort field
     * @param sortDirection Sort direction ("asc" or "desc")
     * @return Pageable object
     */
    public static Pageable convertToPageable(Integer page, Integer size, String sortBy, String sortDirection) {
        Sort sort = null;
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Sort.Direction direction = Sort.Direction.fromString(
                sortDirection != null ? sortDirection.toLowerCase() : "asc"
            );
            sort = Sort.by(direction, sortBy);
        }
        return convertToPageable(page, size, sort);
    }

    /**
     * Convert Spring Data Page object to frontend-friendly format
     * 
     * @param page Spring Data Page object
     * @return Map with converted pagination information
     */
    public static Map<String, Object> convertToFrontendPage(Page<?> page) {
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("content", page.getContent());                // Current page data list
        pageInfo.put("number", page.getNumber() + 1);             // Current page number (starting from 1)
        pageInfo.put("size", page.getSize());                     // Page size
        pageInfo.put("totalElements", page.getTotalElements());   // Total record count
        pageInfo.put("totalPages", page.getTotalPages());         // Total page count
        pageInfo.put("first", page.isFirst());                    // Whether it's the first page
        pageInfo.put("last", page.isLast());                      // Whether it's the last page
        pageInfo.put("empty", page.isEmpty());                    // Whether current page is empty
        
        return pageInfo;
    }
}