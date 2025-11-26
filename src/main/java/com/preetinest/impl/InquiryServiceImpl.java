package com.preetinest.impl;

import com.preetinest.dto.request.InquiryRequestDTO;
import com.preetinest.dto.response.InquiryResponseDTO;
import com.preetinest.entity.*;
import com.preetinest.repository.*;
import com.preetinest.service.InquiryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private static final Logger log = LoggerFactory.getLogger(InquiryServiceImpl.class);

    private final InquiryRepository inquiryRepository;
    private final ServiceRepository serviceRepository;
    private final BlogRepository blogRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Map<String, Object> createInquiry(InquiryRequestDTO dto) {
        log.info("=== CREATE INQUIRY START ===");
        log.info("Incoming inquiry from: {} <{}> | Phone: {}", dto.getName(), dto.getEmail(), dto.getPhone());
        if (dto.getSlug() != null) log.info("Slug provided: {}", dto.getSlug());

        String pageType = "GENERAL";
        String pageName = "Homepage / Contact Form";
        String slug = dto.getSlug() != null ? dto.getSlug().trim() : null;

        // Resolve slug → pageType & pageName
        if (slug != null && !slug.isEmpty()) {
            log.info("Attempting to resolve slug: {}", slug);

            // 1. Check Service
            Optional<Services> serviceOpt = serviceRepository.findBySlug(slug);
            if (serviceOpt.isPresent() && isActiveEntity(serviceOpt.get())) {
                Services service = serviceOpt.get();
                pageType = "SERVICE";
                pageName = service.getName();
                log.info("Slug resolved to SERVICE: {}", pageName);
            }
            // 2. Check Blog
            else {
                Optional<Blog> blogOpt = blogRepository.findBySlug(slug);
                if (blogOpt.isPresent() && isActiveEntity(blogOpt.get())) {
                    Blog blog = blogOpt.get();
                    pageType = "BLOG";
                    pageName = blog.getTitle();
                    log.info("Slug resolved to BLOG: {}", pageName);
                }
                // 3. Check Client
                else {
                    Optional<Clients> clientOpt = clientRepository.findBySlug(slug);
                    if (clientOpt.isPresent() && isActiveEntity(clientOpt.get())) {
                        Clients client = clientOpt.get();
                        pageType = "CLIENT";
                        pageName = client.getName();
                        log.info("Slug resolved to CLIENT: {}", pageName);
                    } else {
                        log.warn("Slug '{}' not found or inactive in Services/Blogs/Clients", slug);
                        pageName = "Unknown Page (slug: " + slug + ")";
                    }
                }
            }
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setUuid(UUID.randomUUID().toString());
        inquiry.setName(dto.getName());
        inquiry.setEmail(dto.getEmail());
        inquiry.setPhone(dto.getPhone());
        inquiry.setLocation(dto.getLocation());
        inquiry.setMessage(dto.getMessage());
        inquiry.setPageName(pageName);
        inquiry.setPageType(pageType);
        inquiry.setSlug(slug != null && !slug.isEmpty() ? slug : null);

// These have smart defaults in DB + entity, but we set them explicitly for clarity and consistency
        inquiry.setActive(true);
        inquiry.setDisplayStatus(true);
        inquiry.setDeleteStatus(2);

// Optional: set createdBy if admin is updating later (not needed on create from public form)
// inquiry.setCreatedBy(null); // usually null on public inquiry

// Let JPA Auditing handle these — do NOT set manually unless you disable auditing
 inquiry.setCreatedAt(LocalDateTime.now());
 inquiry.setUpdatedAt(LocalDateTime.now());

        Inquiry saved = inquiryRepository.save(inquiry);
        log.info("Inquiry created successfully | ID: {} | UUID: {} | Page: {}", saved.getId(), saved.getUuid(), pageName);

        Map<String, Object> response = mapToResponseMap(saved);
        log.info("=== CREATE INQUIRY SUCCESS ===");
        return response;
    }

    @Override
    public Optional<Map<String, Object>> getInquiryById(Long id) {
        log.info("Fetching inquiry by ID: {}", id);
        return inquiryRepository.findById(id)
                .filter(i -> i.getDeleteStatus() == 2 && i.isActive())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getInquiryByUuid(String uuid) {
        log.info("Fetching inquiry by UUID: {}", uuid);
        return inquiryRepository.findByUuid(uuid)
                .filter(i -> i.getDeleteStatus() == 2 && i.isActive())
                .map(this::mapToResponseMap);
    }

    @Override
    public Map<String, Object> getAllActiveInquiries(Long userId, Pageable pageable) {
        log.info("=== FETCH ALL INQUIRIES | Admin userId: {} ===", userId);

        User admin = getAdminUser(userId);

        Page<Inquiry> page = inquiryRepository.findAllActiveInquiries(pageable);

        List<Map<String, Object>> inquiries = page.getContent()
                .stream()
                .map(this::mapToResponseMap)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("inquiries", inquiries);
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("pageSize", page.getSize());
        response.put("hasNext", page.hasNext());
        response.put("hasPrevious", page.hasPrevious());

        log.info("Returned {} inquiries (page {}/{})", inquiries.size(), page.getNumber() + 1, page.getTotalPages());
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> updateInquiry(Long id, InquiryRequestDTO dto, Long userId) {
        log.info("=== UPDATE INQUIRY ID: {} by userId: {} ===", id, userId);

        Inquiry inquiry = inquiryRepository.findById(id)
                .filter(i -> i.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found"));

        getAdminUser(userId); // validates permission

        inquiry.setName(dto.getName());
        inquiry.setEmail(dto.getEmail());
        inquiry.setPhone(dto.getPhone());
        inquiry.setLocation(dto.getLocation());
        inquiry.setMessage(dto.getMessage());
        inquiry.setCreatedBy(userRepository.findById(userId).orElse(null)); // optional

        Inquiry updated = inquiryRepository.save(inquiry);
        log.info("Inquiry ID {} updated successfully", id);

        return mapToResponseMap(updated);
    }

    @Override
    @Transactional
    public void softDeleteInquiry(Long id, Long userId) {
        log.info("=== SOFT DELETE INQUIRY ID: {} by userId: {} ===", id, userId);

        Inquiry inquiry = inquiryRepository.findById(id)
                .filter(i -> i.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found"));

        getAdminUser(userId);

        inquiry.setDeleteStatus(1);
        inquiry.setActive(false);
        inquiry.setDisplayStatus(false);
        inquiryRepository.save(inquiry);

        log.warn("Inquiry ID {} has been soft-deleted by admin {}", id, userId);
    }

    // ====================== HELPERS ======================

    private User getAdminUser(Long userId) {
        if (userId == null) {
            log.error("userId is null – admin access required");
            throw new IllegalArgumentException("User ID is required");
        }

        return userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole().getName()))
                .orElseThrow(() -> {
                    log.error("Access denied: User {} is not an active ADMIN", userId);
                    return new IllegalArgumentException("Only ADMIN users can perform this action");
                });
    }

    private boolean isActiveEntity(Object entity) {
        if (entity instanceof Services s) {
            return s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus();
        }
        if (entity instanceof Blog b) {
            return b.getDeleteStatus() == 2 && b.isActive() && b.isDisplayStatus();
        }
        if (entity instanceof Clients c) {
            return c.getDeleteStatus() == 2 && c.isActive() && c.isDisplayStatus();
        }
        return false;
    }

    // ====================== MAPPER ======================

    private Map<String, Object> mapToResponseMap(Inquiry inquiry) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", inquiry.getId());
        map.put("uuid", inquiry.getUuid());
        map.put("name", inquiry.getName());
        map.put("email", inquiry.getEmail());
        map.put("phone", inquiry.getPhone());
        map.put("location", inquiry.getLocation());
        map.put("message", inquiry.getMessage());
        map.put("pageType", inquiry.getPageType());
        map.put("pageName", inquiry.getPageName());
        map.put("slug", inquiry.getSlug());
        map.put("active", inquiry.isActive());
        map.put("createdAt", inquiry.getCreatedAt());
        map.put("updatedAt", inquiry.getUpdatedAt());
        map.put("createdById", inquiry.getCreatedBy() != null ? inquiry.getCreatedBy().getId() : null);
        return map;
    }
}