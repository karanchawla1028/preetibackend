package com.preetinest.impl;

import com.preetinest.config.S3Service;
import com.preetinest.dto.request.BlogDetailRequestDTO;
import com.preetinest.entity.Blog;
import com.preetinest.entity.BlogDetail;
import com.preetinest.entity.User;
import com.preetinest.repository.BlogDetailRepository;
import com.preetinest.repository.BlogRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.BlogDetailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogDetailServiceImpl implements BlogDetailService {

    private static final Logger log = LoggerFactory.getLogger(BlogDetailServiceImpl.class);

    private final BlogDetailRepository blogDetailRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public Map<String, Object> createBlogDetail(BlogDetailRequestDTO dto, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.info("=== CREATE BLOG DETAIL START ===");
        log.info("Request DTO: {}", dto);

        User admin = getAdminUser(userId);

        Blog blog = blogRepository.findById(dto.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Valid blog not found with ID: " + dto.getBlogId()));

        BlogDetail detail = new BlogDetail();
        detail.setUuid(UUID.randomUUID().toString());
        detail.setHeading(dto.getHeading());
        detail.setContent(dto.getContent());
        detail.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        detail.setBlog(blog);
        detail.setActive(dto.getActive() != null ? dto.getActive() : true);
        detail.setDisplayStatus(dto.getActive() != null ? dto.getActive() : true);
        detail.setDeleteStatus(2);
        detail.setCreatedBy(admin);

        // Handle S3 image (preferred)
        if (dto.getImage() != null && !dto.getImage().trim().isEmpty()) {
            String fileName = dto.getImage().trim();
            detail.setImageUrl(fileName);
            log.info("BlogDetail image set → S3 key: {}", fileName);
        }
        // Fallback: direct external URL (optional support)
        else if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            detail.setImageUrl(dto.getImageUrl().trim());
            log.info("BlogDetail image set → external URL: {}", dto.getImageUrl());
        }

        BlogDetail saved = blogDetailRepository.save(detail);
        log.info("Blog detail created | ID: {} | BlogID: {}", saved.getId(), blog.getId());

        Map<String, Object> response = mapToResponseMap(saved);
        log.info("=== CREATE BLOG DETAIL SUCCESS ===");
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> updateBlogDetail(Long id, BlogDetailRequestDTO dto, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.info("=== UPDATE BLOG DETAIL ID: {} ===", id);

        BlogDetail detail = blogDetailRepository.findById(id)
                .filter(d -> d.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog detail not found with ID: " + id));

        getAdminUser(userId);

        Blog blog = blogRepository.findById(dto.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Valid blog not found"));

        detail.setHeading(dto.getHeading());
        detail.setContent(dto.getContent());
        detail.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : detail.getDisplayOrder());
        detail.setBlog(blog);
        detail.setActive(dto.getActive() != null ? dto.getActive() : detail.isActive());
        detail.setDisplayStatus(dto.getActive() != null ? dto.getActive() : detail.isDisplayStatus());

        // Update image if provided
        if (dto.getImage() != null && !dto.getImage().trim().isEmpty()) {
            detail.setImageUrl(dto.getImage().trim());
            log.info("BlogDetail image updated → S3 key: {}", dto.getImage());
        } else if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            detail.setImageUrl(dto.getImageUrl().trim());
            log.info("BlogDetail image updated → external URL: {}", dto.getImageUrl());
        }

        BlogDetail updated = blogDetailRepository.save(detail);
        log.info("Blog detail updated | ID: {}", updated.getId());

        Map<String, Object> response = mapToResponseMap(updated);
        log.info("=== UPDATE BLOG DETAIL SUCCESS ===");
        return response;
    }

    @Override
    public Optional<Map<String, Object>> getBlogDetailById(Long id) {
        log.info("Fetching blog detail by ID: {}", id);
        return blogDetailRepository.findById(id)
                .filter(d -> d.getDeleteStatus() == 2 && d.isActive() && d.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getBlogDetailByUuid(String uuid) {
        log.info("Fetching blog detail by UUID: {}", uuid);
        return blogDetailRepository.findByUuid(uuid)
                .filter(d -> d.getDeleteStatus() == 2 && d.isActive() && d.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public List<Map<String, Object>> getBlogDetailsByBlogId(Long blogId) {
        log.info("Fetching all details for blog ID: {}", blogId);
        return blogDetailRepository.findByBlogId(blogId)
                .stream()
                .filter(d -> d.getDeleteStatus() == 2 && d.isActive() && d.isDisplayStatus())
                .sorted(Comparator.comparingInt(BlogDetail::getDisplayOrder))
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void softDeleteBlogDetail(Long id, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.info("=== SOFT DELETE BLOG DETAIL ID: {} by userId: {} ===", id, userId);

        BlogDetail detail = blogDetailRepository.findById(id)
                .filter(d -> d.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog detail not found with ID: " + id));

        getAdminUser(userId);

        detail.setDeleteStatus(1);
        detail.setActive(false);
        detail.setDisplayStatus(false);
        blogDetailRepository.save(detail);

        log.warn("Blog detail ID {} soft-deleted by user {}", id, userId);
        log.info("=== SOFT DELETE BLOG DETAIL SUCCESS ===");
    }

    // ====================== HELPER ======================
    private User getAdminUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole().getName()))
                .orElseThrow(() -> new IllegalArgumentException("Only ADMIN users can perform this action"));
    }

    // ====================== MAPPER (with full S3 URL) ======================
    private Map<String, Object> mapToResponseMap(BlogDetail detail) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", detail.getId());
        map.put("uuid", detail.getUuid());
        map.put("heading", detail.getHeading());
        map.put("content", detail.getContent());
        map.put("displayOrder", detail.getDisplayOrder());
        map.put("blogId", detail.getBlog().getId());
        map.put("active", detail.isActive());
        map.put("displayStatus", detail.isDisplayStatus());
        map.put("createdAt", detail.getCreatedAt());
        map.put("updatedAt", detail.getUpdatedAt());
        map.put("createdById", detail.getCreatedBy() != null ? detail.getCreatedBy().getId() : null);

        // Returns full public S3 URL if imageUrl is an S3 key
        String imageKey = detail.getImageUrl();
        map.put("imageUrl", imageKey != null ? s3Service.getFullUrl(imageKey) : null);

        return map;
    }
}