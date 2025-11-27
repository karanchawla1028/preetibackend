package com.preetinest.impl;


import com.preetinest.config.S3Service;
import com.preetinest.dto.request.BlogRequestDTO;
import com.preetinest.entity.*;
import com.preetinest.repository.*;
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

    private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ServiceRepository serviceRepository;
    private final S3Service s3Service;

    @Autowired
    public BlogServiceImpl(
            BlogRepository blogRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            ServiceRepository serviceRepository,
            S3Service s3Service) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.serviceRepository = serviceRepository;
        this.s3Service = s3Service;
    }

    @Override
    @Transactional
    public Map<String, Object> createBlog(BlogRequestDTO requestDTO, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a category");
        }
        log.info("=== CREATE BLOG START ===");

        validateSlug(requestDTO.getSlug(), null);
        User createdBy = getAdminUser(userId);

        Category category = getValidCategory(requestDTO.getCategoryId());
        SubCategory subCategory = requestDTO.getSubCategoryId() != null ? getValidSubCategory(requestDTO.getSubCategoryId()) : null;
        Services service = requestDTO.getServiceId() != null ? getValidService(requestDTO.getServiceId()) : null;

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

        if (requestDTO.getImage() != null && !requestDTO.getImage().isBlank()) {
            String fileName = requestDTO.getImage();
            String fullUrl = s3Service.getFullUrl(fileName);

            blog.setImage(fileName);           // e.g. abc123.png
            blog.setImageUrl(fullUrl);         // https://bucket.s3.../abc123.png
            blog.setThumbnailUrl(fileName);    // optional: keep old field in sync

            log.info("Blog image set → filename: {}, full URL: {}", fileName, fullUrl);
        }

        Blog saved = blogRepository.save(blog);
        log.info("Blog created → ID: {}", saved.getId());

        Map<String, Object> response = mapToResponseMap(saved);
        log.info("=== CREATE BLOG SUCCESS ===");
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> updateBlog(Long id, BlogRequestDTO requestDTO, Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a category");
        }
        log.info("=== UPDATE BLOG ID: {} ===", id);

        Blog blog = blogRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found"));

        validateSlug(requestDTO.getSlug(), id);
        getAdminUser(userId);

        blog.setTitle(requestDTO.getTitle());
        blog.setExcerpt(requestDTO.getExcerpt());
        blog.setMetaTitle(requestDTO.getMetaTitle());
        blog.setMetaKeyword(requestDTO.getMetaKeyword());
        blog.setMetaDescription(requestDTO.getMetaDescription());
        blog.setSlug(requestDTO.getSlug());
        blog.setActive(requestDTO.getActive());
        blog.setDisplayStatus(requestDTO.getActive());
        blog.setShowOnHome(requestDTO.getShowOnHome());
        blog.setCategory(getValidCategory(requestDTO.getCategoryId()));
        blog.setSubCategory(requestDTO.getSubCategoryId() != null ? getValidSubCategory(requestDTO.getSubCategoryId()) : null);
        blog.setService(requestDTO.getServiceId() != null ? getValidService(requestDTO.getServiceId()) : null);

        if (requestDTO.getImage() != null && !requestDTO.getImage().isBlank()) {
            String fileName = requestDTO.getImage();
            String fullUrl = s3Service.getFullUrl(fileName);

            blog.setImage(fileName);
            blog.setImageUrl(fullUrl);
            blog.setThumbnailUrl(fileName);

            log.info("Blog image updated → {}", fullUrl);
        }
        Blog updated = blogRepository.save(blog);
        return mapToResponseMap(updated);
    }

    // Helper methods
    private Category getValidCategory(Long id) {
        return categoryRepository.findById(id)
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    private SubCategory getValidSubCategory(Long id) {
        return subCategoryRepository.findById(id)
                .filter(sc -> sc.getDeleteStatus() == 2 && sc.isActive() && sc.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("SubCategory not found"));
    }

    private Services getValidService(Long id) {
        return serviceRepository.findById(id)
                .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
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

    // ====================== MAPPERS ======================
    private Map<String, Object> mapToResponseMap(Blog blog) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", blog.getId());
        map.put("uuid", blog.getUuid());
        map.put("title", blog.getTitle());
        map.put("excerpt", blog.getExcerpt());
        map.put("metaTitle", blog.getMetaTitle());
        map.put("metaKeyword", blog.getMetaKeyword());
        map.put("metaDescription", blog.getMetaDescription());
        map.put("slug", blog.getSlug());
        map.put("active", blog.isActive());
        map.put("displayStatus", blog.isDisplayStatus());
        map.put("showOnHome", blog.isShowOnHome());
        map.put("createdAt", blog.getCreatedAt());
        map.put("updatedAt", blog.getUpdatedAt());
        map.put("createdById", blog.getCreatedBy() != null ? blog.getCreatedBy().getId() : null);
        map.put("categoryId", blog.getCategory().getId());
        map.put("subCategoryId", blog.getSubCategory() != null ? blog.getSubCategory().getId() : null);
        map.put("serviceId", blog.getService() != null ? blog.getService().getId() : null);

        // Return both for maximum compatibility
        map.put("thumbnailUrl", blog.getThumbnailUrl() != null ? s3Service.getFullUrl(blog.getThumbnailUrl()) : null);
        map.put("image", blog.getImage());                           // filename
        map.put("imageUrl", blog.getImageUrl() != null ? blog.getImageUrl() :
                (blog.getImage() != null ? s3Service.getFullUrl(blog.getImage()) : null));

        return map;
    }

}