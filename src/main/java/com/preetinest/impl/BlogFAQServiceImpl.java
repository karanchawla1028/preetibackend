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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogFAQServiceImpl implements BlogFAQService {

    private static final Logger log = LoggerFactory.getLogger(BlogFAQServiceImpl.class);

    private final BlogFAQRepository blogFAQRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Map<String, Object> createBlogFAQ(BlogFAQRequestDTO dto, Long userId) {
        log.info("=== CREATE BLOG FAQ START ===");
        log.info("Question: {}", dto.getQuestion());
        log.info("Blog ID: {} | Created by userId: {}", dto.getBlogId(), userId);

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a category");
        }

        try {
            User admin = getAdminUser(userId);

            Blog blog = blogRepository.findById(dto.getBlogId())
                    .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                    .orElseThrow(() -> {
                        log.error("Blog not found or inactive. blogId={}", dto.getBlogId());
                        return new EntityNotFoundException("Valid blog not found with ID: " + dto.getBlogId());
                    });

            BlogFAQ faq = new BlogFAQ();
            faq.setUuid(UUID.randomUUID().toString());
            faq.setQuestion(dto.getQuestion());
            faq.setAnswer(dto.getAnswer());
            faq.setBlog(blog);
            faq.setDisplayOrder(dto.getDisplayOrder()); // already @NotNull → safe
            faq.setDisplayOrder(dto.getDisplayOrder()); // safe — @NotNull guarantees non-null
            faq.setActive(dto.getActive() != null ? dto.getActive() : true);
            faq.setDisplayStatus(dto.getDisplayStatus() != null ? dto.getDisplayStatus() : true);
            faq.setDeleteStatus(2);
            faq.setCreatedBy(admin);

            BlogFAQ saved = blogFAQRepository.save(faq);
            log.info("Blog FAQ created successfully | ID: {} | Blog: {}", saved.getId(), blog.getTitle());

            Map<String, Object> response = mapToResponseMap(saved);
            log.info("=== CREATE BLOG FAQ SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR creating Blog FAQ: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateBlogFAQ(Long id, BlogFAQRequestDTO dto, Long userId) {
        log.info("=== UPDATE BLOG FAQ ID: {} ===", id);
        log.info("Updated Question: {}", dto.getQuestion());

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a category");
        }

        try {
            BlogFAQ faq = blogFAQRepository.findById(id)
                    .filter(f -> f.getDeleteStatus() == 2)
                    .orElseThrow(() -> {
                        log.error("Blog FAQ not found. faqId={}", id);
                        return new EntityNotFoundException("Blog FAQ not found with ID: " + id);
                    });

            getAdminUser(userId); // validates permission

            Blog blog = blogRepository.findById(dto.getBlogId())
                    .filter(b -> b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus())
                    .orElseThrow(() -> new EntityNotFoundException("Valid blog not found"));

            faq.setQuestion(dto.getQuestion());
            faq.setAnswer(dto.getAnswer());
            faq.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : faq.getDisplayOrder());
            faq.setBlog(blog);
            faq.setActive(dto.getActive() != null ? dto.getActive() : faq.isActive());
            faq.setDisplayStatus(dto.getDisplayStatus() != null ? dto.getDisplayStatus() : faq.isDisplayStatus());

            BlogFAQ updated = blogFAQRepository.save(faq);
            log.info("Blog FAQ updated successfully | ID: {}", updated.getId());

            Map<String, Object> response = mapToResponseMap(updated);
            log.info("=== UPDATE BLOG FAQ SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR updating Blog FAQ ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<Map<String, Object>> getBlogFAQById(Long id) {
        log.info("Fetching Blog FAQ by ID: {}", id);
        return blogFAQRepository.findById(id)
                .filter(f -> f.getDeleteStatus() == 2 && f.isActive() && f.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getBlogFAQByUuid(String uuid) {
        log.info("Fetching Blog FAQ by UUID: {}", uuid);
        return blogFAQRepository.findByUuid(uuid)
                .filter(f -> f.getDeleteStatus() == 2 && f.isActive() && f.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public List<Map<String, Object>> getBlogFAQsByBlogId(Long blogId) {
        log.info("Fetching all FAQs for Blog ID: {}", blogId);
        return blogFAQRepository.findByBlogId(blogId)
                .stream()
                .filter(f -> f.getDeleteStatus() == 2 && f.isActive() && f.isDisplayStatus())
                .sorted(Comparator.comparingInt(BlogFAQ::getDisplayOrder))
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void softDeleteBlogFAQ(Long id, Long userId) {
        log.info("=== SOFT DELETE BLOG FAQ ID: {} by userId: {} ===", id, userId);

        try {
            BlogFAQ faq = blogFAQRepository.findById(id)
                    .filter(f -> f.getDeleteStatus() == 2)
                    .orElseThrow(() -> {
                        log.error("Blog FAQ not found. faqId={}", id);
                        return new EntityNotFoundException("Blog FAQ not found with ID: " + id);
                    });

            getAdminUser(userId);

            faq.setDeleteStatus(1);
            faq.setActive(false);
            faq.setDisplayStatus(false);
            blogFAQRepository.save(faq);

            log.warn("Blog FAQ ID {} soft-deleted by admin {}", id, userId);
            log.info("=== SOFT DELETE BLOG FAQ SUCCESS ===");

        } catch (Exception e) {
            log.error("ERROR soft-deleting Blog FAQ ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ====================== HELPER ======================

    private User getAdminUser(Long userId) {
        if (userId == null) {
            log.error("userId is null – admin required");
            throw new IllegalArgumentException("User ID is required");
        }

        return userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole().getName()))
                .orElseThrow(() -> {
                    log.error("User {} is not an active ADMIN", userId);
                    return new IllegalArgumentException("Only ADMIN users can perform this action");
                });
    }

    // ====================== MAPPER ======================

    private Map<String, Object> mapToResponseMap(BlogFAQ faq) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", faq.getId());
        map.put("uuid", faq.getUuid());
        map.put("question", faq.getQuestion());
        map.put("answer", faq.getAnswer());
        map.put("displayOrder", faq.getDisplayOrder());
        map.put("blogId", faq.getBlog().getId());
        map.put("active", faq.isActive());
        map.put("displayStatus", faq.isDisplayStatus());
        map.put("createdAt", faq.getCreatedAt());
        map.put("updatedAt", faq.getUpdatedAt());
        map.put("createdById", faq.getCreatedBy() != null ? faq.getCreatedBy().getId() : null);
        return map;
    }
}