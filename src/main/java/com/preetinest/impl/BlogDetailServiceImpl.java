package com.preetinest.impl;

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
    public BlogDetailServiceImpl(BlogDetailRepository blogDetailRepository,
                                 BlogRepository blogRepository,
                                 UserRepository userRepository) {
        this.blogDetailRepository = blogDetailRepository;
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> createBlogDetail(BlogDetailRequestDTO requestDTO, Long userId) {
        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can create blog details");
            }
        }

        Blog blog = blogRepository.findById(requestDTO.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + requestDTO.getBlogId()));

        BlogDetail blogDetail = new BlogDetail();
        blogDetail.setUuid(UUID.randomUUID().toString());
        blogDetail.setHeading(requestDTO.getHeading());
        blogDetail.setContent(requestDTO.getContent());
        blogDetail.setImageUrl(requestDTO.getImageUrl());
        blogDetail.setDisplayOrder(requestDTO.getDisplayOrder());
        blogDetail.setBlog(blog);
        blogDetail.setActive(requestDTO.getActive());
        blogDetail.setDisplayStatus(requestDTO.getActive());
        blogDetail.setDeleteStatus(2);
        blogDetail.setCreatedBy(createdBy);

        BlogDetail savedBlogDetail = blogDetailRepository.save(blogDetail);
        return mapToResponseMap(savedBlogDetail);
    }

    @Override
    public Optional<Map<String, Object>> getBlogDetailById(Long id) {
        return blogDetailRepository.findById(id)
                .filter(bd -> bd.getDeleteStatus() == 2 && bd.isActive() && bd.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getBlogDetailByUuid(String uuid) {
        return blogDetailRepository.findByUuid(uuid)
                .filter(bd -> bd.getDeleteStatus() == 2 && bd.isActive() && bd.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public List<Map<String, Object>> getBlogDetailsByBlogId(Long blogId) {
        return blogDetailRepository.findByBlogId(blogId)
                .stream()
                .filter(bd -> bd.getDeleteStatus() == 2 && bd.isActive() && bd.isDisplayStatus())
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> updateBlogDetail(Long id, BlogDetailRequestDTO requestDTO, Long userId) {
        BlogDetail blogDetail = blogDetailRepository.findById(id)
                .filter(bd -> bd.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog detail not found with id: " + id));

        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can update blog details");
            }
        }

        Blog blog = blogRepository.findById(requestDTO.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + requestDTO.getBlogId()));

        blogDetail.setHeading(requestDTO.getHeading());
        blogDetail.setContent(requestDTO.getContent());
        blogDetail.setImageUrl(requestDTO.getImageUrl());
        blogDetail.setDisplayOrder(requestDTO.getDisplayOrder());
        blogDetail.setBlog(blog);
        blogDetail.setActive(requestDTO.getActive());
        blogDetail.setDisplayStatus(requestDTO.getActive());
        blogDetail.setCreatedBy(createdBy);

        BlogDetail updatedBlogDetail = blogDetailRepository.save(blogDetail);
        return mapToResponseMap(updatedBlogDetail);
    }

    @Override
    public void softDeleteBlogDetail(Long id, Long userId) {
        BlogDetail blogDetail = blogDetailRepository.findById(id)
                .filter(bd -> bd.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog detail not found with id: " + id));

        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN users can delete blog details");
        }

        blogDetail.setDeleteStatus(1);
        blogDetail.setActive(false);
        blogDetail.setDisplayStatus(false);
        blogDetailRepository.save(blogDetail);
    }

    private BlogDetailResponseDTO mapToResponseDTO(BlogDetail blogDetail) {
        BlogDetailResponseDTO dto = new BlogDetailResponseDTO();
        dto.setId(blogDetail.getId());
        dto.setUuid(blogDetail.getUuid());
        dto.setHeading(blogDetail.getHeading());
        dto.setContent(blogDetail.getContent());
        dto.setImageUrl(blogDetail.getImageUrl());
        dto.setDisplayOrder(blogDetail.getDisplayOrder());
        dto.setBlogId(blogDetail.getBlog().getId());
        dto.setActive(blogDetail.isActive());
        dto.setCreatedAt(blogDetail.getCreatedAt());
        dto.setUpdatedAt(blogDetail.getUpdatedAt());
        dto.setCreatedById(blogDetail.getCreatedBy() != null ? blogDetail.getCreatedBy().getId() : null);
        return dto;
    }

    private Map<String, Object> mapToResponseMap(BlogDetail blogDetail) {
        BlogDetailResponseDTO dto = mapToResponseDTO(blogDetail);
        Map<String, Object> response = new HashMap<>();
        response.put("id", dto.getId());
        response.put("uuid", dto.getUuid());
        response.put("heading", dto.getHeading());
        response.put("content", dto.getContent());
        response.put("imageUrl", dto.getImageUrl());
        response.put("displayOrder", dto.getDisplayOrder());
        response.put("blogId", dto.getBlogId());
        response.put("active", dto.isActive());
        response.put("createdAt", dto.getCreatedAt());
        response.put("updatedAt", dto.getUpdatedAt());
        response.put("createdById", dto.getCreatedById());
        return response;
    }
}