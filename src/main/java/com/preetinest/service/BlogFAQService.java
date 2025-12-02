package com.preetinest.service;

import com.preetinest.dto.request.BlogFAQRequestDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BlogFAQService {
    Optional<Map<String, Object>> getBlogFAQById(Long id);
    Optional<Map<String, Object>> getBlogFAQByUuid(String uuid);
    List<Map<String, Object>> getBlogFAQsByBlogId(Long blogId);
    void softDeleteBlogFAQ(Long id, Long userId);
    Map<String, Object> createBlogFAQ(BlogFAQRequestDTO requestDTO, Long userId);
    Map<String, Object> updateBlogFAQ(Long id, BlogFAQRequestDTO requestDTO, Long userId);
}
