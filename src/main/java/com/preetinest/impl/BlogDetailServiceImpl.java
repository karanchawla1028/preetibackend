package com.preetinest.impl;

import com.preetinest.config.S3Service;
import com.preetinest.dto.request.BlogDetailRequestDTO;
import com.preetinest.dto.response.BlogDetailResponseDTO;
import com.preetinest.entity.Blog;
import com.preetinest.entity.BlogDetail;
import com.preetinest.entity.User;
import com.preetinest.repository.BlogDetailRepository;
import com.preetinest.repository.BlogRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.BlogDetailService;
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
public class BlogDetailServiceImpl implements BlogDetailService {

    private final BlogDetailRepository blogDetailRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    public BlogDetailServiceImpl(BlogDetailRepository blogDetailRepository,
                                 BlogRepository blogRepository,
                                 UserRepository userRepository) {
        this.blogDetailRepository = blogDetailRepository;
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> createBlogDetail(BlogDetailRequestDTO requestDTO, Long userId) {
        User createdBy = getAdminUser(userId);

        Blog blog = blogRepository.findById(requestDTO.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Blog not found"));

        BlogDetail blogDetail = new BlogDetail();
        blogDetail.setUuid(UUID.randomUUID().toString());
        blogDetail.setHeading(requestDTO.getHeading());
        blogDetail.setContent(requestDTO.getContent());
        blogDetail.setDisplayOrder(requestDTO.getDisplayOrder());
        blogDetail.setBlog(blog);
        blogDetail.setActive(requestDTO.getActive());
        blogDetail.setDisplayStatus(requestDTO.getActive());
        blogDetail.setDeleteStatus(2);
        blogDetail.setCreatedBy(createdBy);

        // Upload image directly to root
        if (requestDTO.getImageBase64() != null && !requestDTO.getImageBase64().isBlank()) {
            String fileName = s3Service.uploadBase64Image(requestDTO.getImageBase64());
            blogDetail.setImageUrl(fileName);
        }

        BlogDetail saved = blogDetailRepository.save(blogDetail);
        return mapToResponseMap(saved);
    }

    @Override
    public Map<String, Object> updateBlogDetail(Long id, BlogDetailRequestDTO requestDTO, Long userId) {
        BlogDetail blogDetail = blogDetailRepository.findById(id)
                .filter(bd -> bd.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog detail not found"));

        getAdminUser(userId);

        Blog blog = blogRepository.findById(requestDTO.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Blog not found"));

        blogDetail.setHeading(requestDTO.getHeading());
        blogDetail.setContent(requestDTO.getContent());
        blogDetail.setDisplayOrder(requestDTO.getDisplayOrder());
        blogDetail.setBlog(blog);
        blogDetail.setActive(requestDTO.getActive());
        blogDetail.setDisplayStatus(requestDTO.getActive());

        if (requestDTO.getImageBase64() != null && !requestDTO.getImageBase64().isBlank()) {
            String fileName = s3Service.uploadBase64Image(requestDTO.getImageBase64());
            blogDetail.setImageUrl(fileName);
        }

        BlogDetail updated = blogDetailRepository.save(blogDetail);
        return mapToResponseMap(updated);
    }

    // Other methods unchanged...
    @Override public Optional<Map<String, Object>> getBlogDetailById(Long id) {
        return blogDetailRepository.findById(id)
                .filter(bd -> bd.getDeleteStatus() == 2 && bd.isActive() && bd.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override public Optional<Map<String, Object>> getBlogDetailByUuid(String uuid) {
        return blogDetailRepository.findByUuid(uuid)
                .filter(bd -> bd.getDeleteStatus() == 2 && bd.isActive() && bd.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override public List<Map<String, Object>> getBlogDetailsByBlogId(Long blogId) {
        return blogDetailRepository.findByBlogId(blogId).stream()
                .filter(bd -> bd.getDeleteStatus() == 2 && bd.isActive() && bd.isDisplayStatus())
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override public void softDeleteBlogDetail(Long id, Long userId) {
        BlogDetail bd = blogDetailRepository.findById(id)
                .filter(b -> b.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        getAdminUser(userId);
        bd.setDeleteStatus(1); bd.setActive(false); bd.setDisplayStatus(false);
        blogDetailRepository.save(bd);
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

    private Map<String, Object> mapToResponseMap(BlogDetail bd) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", bd.getId());
        map.put("uuid", bd.getUuid());
        map.put("heading", bd.getHeading());
        map.put("content", bd.getContent());
        map.put("displayOrder", bd.getDisplayOrder());
        map.put("blogId", bd.getBlog().getId());
        map.put("active", bd.isActive());
        map.put("createdAt", bd.getCreatedAt());
        map.put("updatedAt", bd.getUpdatedAt());
        map.put("createdById", bd.getCreatedBy() != null ? bd.getCreatedBy().getId() : null);

        // THIS IS WHAT YOU WANT
        map.put("imageUrl", s3Service.getFullUrl(bd.getImageUrl()));
        // → https://preetinest.s3.ca-central-1.amazonaws.com/abc123.png

        return map;
    }
}