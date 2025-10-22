package com.preetinest.impl;

import com.preetinest.dto.request.BlogFAQRequestDTO;
import com.preetinest.dto.response.BlogFAQResponseDTO;
import com.preetinest.entity.Blog;
import com.preetinest.entity.BlogFAQ;
import com.preetinest.entity.User;
import com.preetinest.repository.BlogFAQRepository;
import com.preetinest.repository.BlogRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.BlogFAQService;
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
public class BlogFAQServiceImpl implements BlogFAQService {

    private final BlogFAQRepository blogFAQRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Autowired
    public BlogFAQServiceImpl(BlogFAQRepository blogFAQRepository, BlogRepository blogRepository, UserRepository userRepository) {
        this.blogFAQRepository = blogFAQRepository;
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> createBlogFAQ(BlogFAQRequestDTO requestDTO, Long userId) {
        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can create blog FAQs");
            }
        }

        Blog blog = blogRepository.findById(requestDTO.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + requestDTO.getBlogId()));

        BlogFAQ blogFAQ = new BlogFAQ();
        blogFAQ.setUuid(UUID.randomUUID().toString());
        blogFAQ.setQuestion(requestDTO.getQuestion());
        blogFAQ.setAnswer(requestDTO.getAnswer());
        blogFAQ.setDisplayOrder(requestDTO.getDisplayOrder());
        blogFAQ.setBlog(blog);
        blogFAQ.setActive(requestDTO.isActive());
        blogFAQ.setDisplayStatus(requestDTO.isDisplayStatus());
        blogFAQ.setDeleteStatus(2);
        blogFAQ.setCreatedBy(createdBy);

        BlogFAQ savedBlogFAQ = blogFAQRepository.save(blogFAQ);
        return mapToResponseMap(savedBlogFAQ);
    }

    @Override
    public Optional<Map<String, Object>> getBlogFAQById(Long id) {
        return blogFAQRepository.findById(id)
                .filter(bf -> bf.getDeleteStatus() == 2 && bf.isActive() && bf.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getBlogFAQByUuid(String uuid) {
        return blogFAQRepository.findByUuid(uuid)
                .filter(bf -> bf.getDeleteStatus() == 2 && bf.isActive() && bf.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public List<Map<String, Object>> getBlogFAQsByBlogId(Long blogId) {
        return blogFAQRepository.findByBlogId(blogId)
                .stream()
                .filter(bf -> bf.getDeleteStatus() == 2 && bf.isActive() && bf.isDisplayStatus())
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> updateBlogFAQ(Long id, BlogFAQRequestDTO requestDTO, Long userId) {
        BlogFAQ blogFAQ = blogFAQRepository.findById(id)
                .filter(bf -> bf.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog FAQ not found with id: " + id));

        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can update blog FAQs");
            }
        }

        Blog blog = blogRepository.findById(requestDTO.getBlogId())
                .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with id: " + requestDTO.getBlogId()));

        blogFAQ.setQuestion(requestDTO.getQuestion());
        blogFAQ.setAnswer(requestDTO.getAnswer());
        blogFAQ.setDisplayOrder(requestDTO.getDisplayOrder());
        blogFAQ.setBlog(blog);
        blogFAQ.setActive(requestDTO.isActive());
        blogFAQ.setDisplayStatus(requestDTO.isDisplayStatus());
        blogFAQ.setCreatedBy(createdBy);

        BlogFAQ updatedBlogFAQ = blogFAQRepository.save(blogFAQ);
        return mapToResponseMap(updatedBlogFAQ);
    }

    @Override
    public void softDeleteBlogFAQ(Long id, Long userId) {
        BlogFAQ blogFAQ = blogFAQRepository.findById(id)
                .filter(bf -> bf.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Blog FAQ not found with id: " + id));

        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN users can delete blog FAQs");
        }

        blogFAQ.setDeleteStatus(1);
        blogFAQ.setActive(false);
        blogFAQ.setDisplayStatus(false);
        blogFAQRepository.save(blogFAQ);
    }

    private BlogFAQResponseDTO mapToResponseDTO(BlogFAQ blogFAQ) {
        BlogFAQResponseDTO dto = new BlogFAQResponseDTO();
        dto.setId(blogFAQ.getId());
        dto.setUuid(blogFAQ.getUuid());
        dto.setQuestion(blogFAQ.getQuestion());
        dto.setAnswer(blogFAQ.getAnswer());
        dto.setDisplayOrder(blogFAQ.getDisplayOrder());
        dto.setBlogId(blogFAQ.getBlog().getId());
        dto.setActive(blogFAQ.isActive());
        dto.setDisplayStatus(blogFAQ.isDisplayStatus());
        dto.setCreatedAt(blogFAQ.getCreatedAt());
        dto.setUpdatedAt(blogFAQ.getUpdatedAt());
        dto.setCreatedById(blogFAQ.getCreatedBy() != null ? blogFAQ.getCreatedBy().getId() : null);
        return dto;
    }

    private Map<String, Object> mapToResponseMap(BlogFAQ blogFAQ) {
        BlogFAQResponseDTO dto = mapToResponseDTO(blogFAQ);
        Map<String, Object> response = new HashMap<>();
        response.put("id", dto.getId());
        response.put("uuid", dto.getUuid());
        response.put("question", dto.getQuestion());
        response.put("answer", dto.getAnswer());
        response.put("displayOrder", dto.getDisplayOrder());
        response.put("blogId", dto.getBlogId());
        response.put("active", dto.isActive());
        response.put("displayStatus", dto.isDisplayStatus());
        response.put("createdAt", dto.getCreatedAt());
        response.put("updatedAt", dto.getUpdatedAt());
        response.put("createdById", dto.getCreatedById());
        return response;
    }
}
