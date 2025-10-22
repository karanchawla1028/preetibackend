package com.preetinest.impl;

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
        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can create blogs");
            }
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));

        SubCategory subCategory = null;
        if (requestDTO.getSubCategoryId() != null) {
            subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                    .filter(sc -> sc.getDeleteStatus() == 2 && sc.isActive() && sc.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("SubCategory not found with id: " + requestDTO.getSubCategoryId()));
        }

        Services service = null;
        if (requestDTO.getServiceId() != null) {
            service = serviceRepository.findById(requestDTO.getServiceId())
                    .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found with id: " + requestDTO.getServiceId()));
        }

        Blog blog = new Blog();
        blog.setUuid(UUID.randomUUID().toString());
        blog.setTitle(requestDTO.getTitle());
        blog.setExcerpt(requestDTO.getExcerpt());
        blog.setMetaTitle(requestDTO.getMetaTitle());
        blog.setMetaKeyword(requestDTO.getMetaKeyword());
        blog.setMetaDescription(requestDTO.getMetaDescription());
        blog.setSlug(requestDTO.getSlug());
        blog.setThumbnailUrl(requestDTO.getThumbnailUrl());
        blog.setActive(requestDTO.getActive());
        blog.setDisplayStatus(requestDTO.getActive());
        blog.setShowOnHome(requestDTO.getShowOnHome());
        blog.setDeleteStatus(2);
        blog.setCreatedBy(createdBy);
        blog.setCategory(category);
        blog.setSubCategory(subCategory);
        blog.setService(service);

        Blog savedBlog = blogRepository.save(blog);
        return mapToResponseMap(savedBlog);
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
    public Map<String, Object> updateBlog(Long id, BlogRequestDTO requestDTO, Long userId) {
        Blog blog = blogRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + id));

        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can update blogs");
            }
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));

        SubCategory subCategory = null;
        if (requestDTO.getSubCategoryId() != null) {
            subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                    .filter(sc -> sc.getDeleteStatus() == 2 && sc.isActive() && sc.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("SubCategory not found with id: " + requestDTO.getSubCategoryId()));
        }

        Services service = null;
        if (requestDTO.getServiceId() != null) {
            service = serviceRepository.findById(requestDTO.getServiceId())
                    .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found with id: " + requestDTO.getServiceId()));
        }

        blog.setTitle(requestDTO.getTitle());
        blog.setExcerpt(requestDTO.getExcerpt());
        blog.setMetaTitle(requestDTO.getMetaTitle());
        blog.setMetaKeyword(requestDTO.getMetaKeyword());
        blog.setMetaDescription(requestDTO.getMetaDescription());
        blog.setSlug(requestDTO.getSlug());
        blog.setThumbnailUrl(requestDTO.getThumbnailUrl());
        blog.setActive(requestDTO.getActive());
        blog.setDisplayStatus(requestDTO.getActive());
        blog.setShowOnHome(requestDTO.getShowOnHome());
        blog.setCreatedBy(createdBy);
        blog.setCategory(category);
        blog.setSubCategory(subCategory);
        blog.setService(service);

        Blog updatedBlog = blogRepository.save(blog);
        return mapToResponseMap(updatedBlog);
    }

    @Override
    public void softDeleteBlog(Long id, Long userId) {
        Blog blog = blogRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + id));

        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN users can delete blogs");
        }

        blog.setDeleteStatus(1);
        blog.setActive(false);
        blog.setDisplayStatus(false);
        blogRepository.save(blog);
    }

    private BlogResponseDTO mapToResponseDTO(Blog blog) {
        BlogResponseDTO dto = new BlogResponseDTO();
        dto.setId(blog.getId());
        dto.setUuid(blog.getUuid());
        dto.setTitle(blog.getTitle());
        dto.setExcerpt(blog.getExcerpt());
        dto.setMetaTitle(blog.getMetaTitle());
        dto.setMetaKeyword(blog.getMetaKeyword());
        dto.setMetaDescription(blog.getMetaDescription());
        dto.setSlug(blog.getSlug());
        dto.setThumbnailUrl(blog.getThumbnailUrl());
        dto.setActive(blog.isActive());
        dto.setDisplayStatus(blog.isDisplayStatus());
        dto.setShowOnHome(blog.isShowOnHome());
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setUpdatedAt(blog.getUpdatedAt());
        dto.setCreatedById(blog.getCreatedBy() != null ? blog.getCreatedBy().getId() : null);
        dto.setCategoryId(blog.getCategory().getId());
        dto.setSubCategoryId(blog.getSubCategory() != null ? blog.getSubCategory().getId() : null);
        dto.setServiceId(blog.getService() != null ? blog.getService().getId() : null);
        return dto;
    }

    private Map<String, Object> mapToResponseMap(Blog blog) {
        BlogResponseDTO dto = mapToResponseDTO(blog);
        Map<String, Object> response = new HashMap<>();
        response.put("id", dto.getId());
        response.put("uuid", dto.getUuid());
        response.put("title", dto.getTitle());
        response.put("excerpt", dto.getExcerpt());
        response.put("metaTitle", dto.getMetaTitle());
        response.put("metaKeyword", dto.getMetaKeyword());
        response.put("metaDescription", dto.getMetaDescription());
        response.put("slug", dto.getSlug());
        response.put("thumbnailUrl", dto.getThumbnailUrl());
        response.put("active", dto.isActive());
        response.put("displayStatus", dto.isDisplayStatus());
        response.put("showOnHome", dto.isShowOnHome());
        response.put("createdAt", dto.getCreatedAt());
        response.put("updatedAt", dto.getUpdatedAt());
        response.put("createdById", dto.getCreatedById());
        response.put("categoryId", dto.getCategoryId());
        response.put("subCategoryId", dto.getSubCategoryId());
        response.put("serviceId", dto.getServiceId());
        return response;
    }
}