package com.preetinest.impl;

import com.preetinest.config.S3Service;
import com.preetinest.dto.request.BlogRequestDTO;
import com.preetinest.entity.Blog;
import com.preetinest.entity.Category;
import com.preetinest.entity.Services;
import com.preetinest.entity.SubCategory;
import com.preetinest.entity.User;
import com.preetinest.repository.BlogRepository;
import com.preetinest.repository.CategoryRepository;
import com.preetinest.repository.ServiceRepository;
import com.preetinest.repository.SubCategoryRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.BlogService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    // ← FIXED: Logger now works!
    private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ServiceRepository serviceRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    public BlogServiceImpl(BlogRepository blogRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           SubCategoryRepository subCategoryRepository,
                           ServiceRepository serviceRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.serviceRepository = serviceRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createBlog(BlogRequestDTO requestDTO, Long userId) {
        log.info("=== CREATE BLOG START ===");
        log.info("Request DTO: {}", requestDTO);
        log.info("Created by userId: {}", userId);

        try {
            validateSlug(requestDTO.getSlug(), null);
            User createdBy = getAdminUser(userId);

            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .filter(c -> c.getDeleteStatus() == 2 && c.isActive())
                    .orElseThrow(() -> {
                        log.error("Category not found or inactive/deleted. categoryId={}", requestDTO.getCategoryId());
                        return new EntityNotFoundException("Valid category not found with ID: " + requestDTO.getCategoryId());
                    });

            SubCategory subCategory = null;
            if (requestDTO.getSubCategoryId() != null) {
                subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                        .filter(sc -> sc.getDeleteStatus() == 2 && sc.isActive() && sc.isDisplayStatus())
                        .orElseThrow(() -> {
                            log.error("SubCategory not found or not displayable. subCategoryId={}", requestDTO.getSubCategoryId());
                            return new EntityNotFoundException("Valid subCategory not found with ID: " + requestDTO.getSubCategoryId());
                        });
            }

            Services service = null;
            if (requestDTO.getServiceId() != null) {
                service = serviceRepository.findById(requestDTO.getServiceId())
                        .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                        .orElseThrow(() -> {
                            log.error("Service not found or not displayable. serviceId={}", requestDTO.getServiceId());
                            return new EntityNotFoundException("Valid service not found with ID: " + requestDTO.getServiceId());
                        });
            }

            Blog blog = new Blog();
            blog.setUuid(UUID.randomUUID().toString());
            blog.setTitle(requestDTO.getTitle());
            blog.setExcerpt(requestDTO.getExcerpt());
            blog.setMetaTitle(requestDTO.getMetaTitle());
            blog.setMetaKeyword(requestDTO.getMetaKeyword());
            blog.setMetaDescription(requestDTO.getMetaDescription());
            blog.setSlug(requestDTO.getSlug());
            blog.setActive(requestDTO.getActive());
            blog.setDisplayStatus(requestDTO.getActive());
            blog.setShowOnHome(requestDTO.getShowOnHome());
            blog.setDeleteStatus(2);
            blog.setCreatedBy(createdBy);
            blog.setCategory(category);
            blog.setSubCategory(subCategory);
            blog.setService(service);

            // FIXED & LOGGED
            if (requestDTO.getThumbnailImageBase64() != null && !requestDTO.getThumbnailImageBase64().isBlank()) {
                String fileName = s3Service.uploadBase64Image(requestDTO.getThumbnailImageBase64());
                blog.setThumbnailUrl(fileName);
                log.info("ThumbnailUrl saved: {}", fileName);
            }

            Blog saved = blogRepository.save(blog);
            log.info("Blog created successfully with ID: {} and UUID: {}", saved.getId(), saved.getUuid());

            Map<String, Object> response = mapToResponseMap(saved);
            log.info("=== CREATE BLOG SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR creating blog: {}", e.getMessage(), e);
            throw e; // re-throw so controller returns 400/500
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateBlog(Long id, BlogRequestDTO requestDTO, Long userId) {
        log.info("=== UPDATE BLOG ID: {} ===", id);
        log.info("Update DTO: {}", requestDTO);

        try {
            Blog blog = blogRepository.findById(id)
                    .filter(b -> b.getDeleteStatus() == 2)
                    .orElseThrow(() -> {
                        log.error("Blog not found or already deleted. blogId={}", id);
                        return new EntityNotFoundException("Blog not found with ID: " + id);
                    });

            validateSlug(requestDTO.getSlug(), id);
            getAdminUser(userId);

            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .filter(c -> c.getDeleteStatus() == 2 && c.isActive())
                    .orElseThrow(() -> {
                        log.error("Category not found or inactive/deleted. categoryId={}", requestDTO.getCategoryId());
                        return new EntityNotFoundException("Valid category not found with ID: " + requestDTO.getCategoryId());
                    });

            SubCategory subCategory = null;
            if (requestDTO.getSubCategoryId() != null) {
                subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                        .filter(sc -> sc.getDeleteStatus() == 2 && sc.isActive() && sc.isDisplayStatus())
                        .orElseThrow(() -> {
                            log.error("SubCategory not found or not displayable. subCategoryId={}", requestDTO.getSubCategoryId());
                            return new EntityNotFoundException("Valid subCategory not found with ID: " + requestDTO.getSubCategoryId());
                        });
            }

            Services service = null;
            if (requestDTO.getServiceId() != null) {
                service = serviceRepository.findById(requestDTO.getServiceId())
                        .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                        .orElseThrow(() -> {
                            log.error("Service not found or not displayable. serviceId={}", requestDTO.getServiceId());
                            return new EntityNotFoundException("Valid service not found with ID: " + requestDTO.getServiceId());
                        });
            }

            blog.setTitle(requestDTO.getTitle());
            blog.setExcerpt(requestDTO.getExcerpt());
            blog.setMetaTitle(requestDTO.getMetaTitle());
            blog.setMetaKeyword(requestDTO.getMetaKeyword());
            blog.setMetaDescription(requestDTO.getMetaDescription());
            blog.setSlug(requestDTO.getSlug());
            blog.setActive(requestDTO.getActive());
            blog.setDisplayStatus(requestDTO.getActive());
            blog.setShowOnHome(requestDTO.getShowOnHome());
            blog.setCategory(category);
            blog.setSubCategory(subCategory);
            blog.setService(service);

            if (requestDTO.getThumbnailImageBase64() != null && !requestDTO.getThumbnailImageBase64().isBlank()) {
                String newFileName = s3Service.uploadBase64Image(requestDTO.getThumbnailImageBase64());
                blog.setThumbnailUrl(newFileName);
                log.info("Updated ThumbnailUrl: {}", newFileName);
            }

            Blog updated = blogRepository.save(blog);
            log.info("Blog updated successfully | ID: {}", updated.getId());

            Map<String, Object> response = mapToResponseMap(updated);
            log.info("=== UPDATE BLOG SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR updating blog ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<Map<String, Object>> getBlogById(Long id) {
        log.info("Fetching blog by ID: {}", id);
        return blogRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(blog -> {
                    log.info("Found blog: {} | ThumbnailUrl: {}", blog.getTitle(), blog.getThumbnailUrl());
                    return mapToResponseMap(blog);
                });
    }

    @Override
    public Optional<Map<String, Object>> getBlogByUuid(String uuid) {
        log.info("Fetching blog by UUID: {}", uuid);
        return blogRepository.findByUuid(uuid)
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getBlogBySlug(String slug) {
        log.info("Fetching blog by slug: {}", slug);
        return blogRepository.findBySlug(slug)
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(blog -> {
                    log.info("Found blog: {} | ThumbnailUrl: {}", blog.getTitle(), blog.getThumbnailUrl());
                    return mapToResponseMap(blog);
                });
    }

    @Override
    public List<Map<String, Object>> getAllActiveBlogs() {
        log.info("Fetching all active blogs");
        return blogRepository.findAllActiveBlogs()
                .stream()
                .filter(b -> b.isActive() && b.isDisplayStatus())
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getBlogsByServiceId(Long serviceId) {
        log.info("Fetching blogs by service ID: {}", serviceId);
        return blogRepository.findByServiceId(serviceId)
                .stream()
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    public void softDeleteBlog(Long id, Long userId) {
        log.info("=== SOFT DELETE BLOG ID: {} by userId: {} ===", id, userId);

        try {
            Blog blog = blogRepository.findById(id)
                    .filter(b -> b.getDeleteStatus() == 2)
                    .orElseThrow(() -> {
                        log.error("Blog not found or already deleted. blogId={}", id);
                        return new EntityNotFoundException("Blog not found with ID: " + id);
                    });

            getAdminUser(userId);

            blog.setDeleteStatus(1);
            blog.setActive(false);
            blog.setDisplayStatus(false);
            blog.setShowOnHome(false);
            blogRepository.save(blog);

            log.warn("Blog ID {} has been soft-deleted by user {}", id, userId);
            log.info("=== SOFT DELETE BLOG SUCCESS ===");

        } catch (Exception e) {
            log.error("ERROR soft-deleting blog ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ====================== HELPERS ======================

    private User getAdminUser(Long userId) {
        if (userId == null) {
            log.warn("userId is null");
            throw new IllegalArgumentException("User ID is required");
        }
        return userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .map(user -> {
                    if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
                        log.error("User {} is not ADMIN", userId);
                        throw new IllegalArgumentException("Only ADMIN can perform this action");
                    }
                    log.info("Admin user verified: {}", user.getEmail());
                    return user;
                })
                .orElseThrow(() -> {
                    log.error("Admin user not found or disabled: {}", userId);
                    return new EntityNotFoundException("Valid admin user not found");
                });
    }

    private void validateSlug(String slug, Long excludeId) {
        blogRepository.findBySlug(slug).ifPresent(existing -> {
            if (existing.getDeleteStatus() == 2 && (excludeId == null || !existing.getId().equals(excludeId))) {
                log.error("Duplicate slug detected: '{}' (existing ID: {})", slug, existing.getId());
                throw new IllegalArgumentException("Slug '" + slug + "' already exists");
            }
        });
    }

    // ====================== MAPPERS (FULL S3 URLS) ======================

    private Map<String, Object> mapToResponseMap(Blog blog) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", blog.getId());
        response.put("uuid", blog.getUuid());
        response.put("title", blog.getTitle());
        response.put("excerpt", blog.getExcerpt());
        response.put("metaTitle", blog.getMetaTitle());
        response.put("metaKeyword", blog.getMetaKeyword());
        response.put("metaDescription", blog.getMetaDescription());
        response.put("slug", blog.getSlug());
        response.put("active", blog.isActive());
        response.put("displayStatus", blog.isDisplayStatus());
        response.put("showOnHome", blog.isShowOnHome());
        response.put("createdAt", blog.getCreatedAt());
        response.put("updatedAt", blog.getUpdatedAt());
        response.put("createdById", blog.getCreatedBy() != null ? blog.getCreatedBy().getId() : null);
        response.put("categoryId", blog.getCategory().getId());
        response.put("subCategoryId", blog.getSubCategory() != null ? blog.getSubCategory().getId() : null);
        response.put("serviceId", blog.getService() != null ? blog.getService().getId() : null);

        // THIS IS WHAT YOU WANT
        String thumbnailKey = blog.getThumbnailUrl();
        response.put("thumbnailUrl", thumbnailKey != null ? s3Service.getFullUrl(thumbnailKey) : null);
        // Example: https://preetinest.s3.ca-central-1.amazonaws.com/abc123def456.png

        return response;
    }
}