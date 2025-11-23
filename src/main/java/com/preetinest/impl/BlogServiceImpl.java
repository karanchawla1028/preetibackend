package com.preetinest.impl;

import com.preetinest.config.S3Service;
import com.preetinest.dto.request.BlogRequestDTO;
import com.preetinest.dto.response.BlogResponseDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

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
    public Map<String, Object> createBlog(BlogRequestDTO requestDTO, Long userId) {
        User createdBy = getAdminUser(userId);

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        SubCategory subCategory = null;
        if (requestDTO.getSubCategoryId() != null) {
            subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                    .filter(sc -> sc.getDeleteStatus() == 2 && sc.isActive() && sc.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("SubCategory not found"));
        }

        Services service = null;
        if (requestDTO.getServiceId() != null) {
            service = serviceRepository.findById(requestDTO.getServiceId())
                    .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found"));
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

        // Upload thumbnail directly to root (no folder)
        if (requestDTO.getThumbnailImageBase64() != null && !requestDTO.getThumbnailImageBase64().isBlank()) {
            String fileName = s3Service.uploadBase64Image(requestDTO.getThumbnailImageBase64());
            blog.setThumbnailUrl(fileName);
        }

        Blog savedBlog = blogRepository.save(blog);
        return mapToResponseMap(savedBlog);
    }

    @Override
    public Map<String, Object> updateBlog(Long id, BlogRequestDTO requestDTO, Long userId) {
        Blog blog = blogRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found"));

        getAdminUser(userId);

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        SubCategory subCategory = null;
        if (requestDTO.getSubCategoryId() != null) {
            subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                    .filter(sc -> sc.getDeleteStatus() == 2 && sc.isActive() && sc.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("SubCategory not found"));
        }

        Services service = null;
        if (requestDTO.getServiceId() != null) {
            service = serviceRepository.findById(requestDTO.getServiceId())
                    .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found"));
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
        }

        Blog updatedBlog = blogRepository.save(blog);
        return mapToResponseMap(updatedBlog);
    }

    @Override
    public Optional<Map<String, Object>> getBlogById(Long id) {
        return blogRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getBlogByUuid(String uuid) {
        return blogRepository.findByUuid(uuid)
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getBlogBySlug(String slug) {
        return blogRepository.findBySlug(slug)
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public List<Map<String, Object>> getAllActiveBlogs() {
        return blogRepository.findAllActiveBlogs()
                .stream()
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getBlogsByServiceId(Long serviceId) {
        return blogRepository.findByServiceId(serviceId)
                .stream()
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    public void softDeleteBlog(Long id, Long userId) {
        Blog blog = blogRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found"));

        getAdminUser(userId);

        blog.setDeleteStatus(1);
        blog.setActive(false);
        blog.setDisplayStatus(false);
        blogRepository.save(blog);
    }

    private User getAdminUser(Long userId) {
        if (userId == null) return null;
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN can perform this action");
        }
        return user;
    }

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
        response.put("thumbnailUrl", s3Service.getFullUrl(blog.getThumbnailUrl()));
        // Example: https://preetinest.s3.ca-central-1.amazonaws.com/abc123def456.png

        return response;
    }
}