package com.preetinest.service;

import com.preetinest.dto.request.BlogRequestDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BlogService {
    Optional<Map<String, Object>> getBlogById(Long id);
    Optional<Map<String, Object>> getBlogByUuid(String uuid);
    Optional<Map<String, Object>> getBlogBySlug(String slug);
    List<Map<String, Object>> getAllActiveBlogs();
    List<Map<String, Object>> getBlogsByServiceId(Long serviceId);
    void softDeleteBlog(Long id, Long userId);
    Map<String, Object> createBlog(BlogRequestDTO requestDTO, Long userId);
    Map<String, Object> updateBlog(Long id, BlogRequestDTO requestDTO, Long userId);
}