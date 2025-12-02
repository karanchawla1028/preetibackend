package com.preetinest.service;

import com.preetinest.dto.request.BlogDetailRequestDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BlogDetailService {
    Optional<Map<String, Object>> getBlogDetailById(Long id);
    Optional<Map<String, Object>> getBlogDetailByUuid(String uuid);
    List<Map<String, Object>> getBlogDetailsByBlogId(Long blogId);
    void softDeleteBlogDetail(Long id, Long userId);
    Map<String, Object> createBlogDetail(BlogDetailRequestDTO requestDTO, Long userId);
    Map<String, Object> updateBlogDetail(Long id, BlogDetailRequestDTO requestDTO, Long userId);
}