package com.preetinest.impl;

import com.preetinest.dto.request.ServiceFAQRequestDTO;
import com.preetinest.dto.response.ServiceFAQResponseDTO;
import com.preetinest.entity.ServiceFAQ;
import com.preetinest.entity.Services;
import com.preetinest.entity.User;
import com.preetinest.repository.ServiceFAQRepository;
import com.preetinest.repository.ServiceRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.ServiceFAQService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceFAQServiceImpl implements ServiceFAQService {

    private static final Logger log = LoggerFactory.getLogger(ServiceFAQServiceImpl.class);

    private final ServiceFAQRepository serviceFAQRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ServiceFAQServiceImpl(ServiceFAQRepository serviceFAQRepository,
                                 ServiceRepository serviceRepository,
                                 UserRepository userRepository) {
        this.serviceFAQRepository = serviceFAQRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Map<String, Object> createServiceFAQ(ServiceFAQRequestDTO dto, Long userId) {
        log.info("=== CREATE SERVICE FAQ START ===");
        log.info("Request DTO: {}", dto);
        log.info("Created by userId: {}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a category");
        }

        try {
            User admin = getAdminUser(userId);

            Services service = serviceRepository.findById(dto.getServiceId())
                    .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                    .orElseThrow(() -> {
                        log.error("Service not found or not active/displayable. serviceId={}", dto.getServiceId());
                        return new EntityNotFoundException("Valid service not found with ID: " + dto.getServiceId());
                    });

            ServiceFAQ faq = new ServiceFAQ();
            faq.setUuid(UUID.randomUUID().toString());
            faq.setQuestion(dto.getQuestion());
            faq.setAnswer(dto.getAnswer());
            faq.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
            faq.setService(service);
            faq.setActive(dto.isActive());
            faq.setDisplayStatus(dto.isDisplayStatus());
            faq.setDeleteStatus(2);
            faq.setCreatedBy(admin);

            ServiceFAQ saved = serviceFAQRepository.save(faq);
            log.info("Service FAQ created successfully | ID: {} | ServiceID: {}", saved.getId(), service.getId());

            Map<String, Object> response = mapToResponseMap(saved);
            log.info("=== CREATE SERVICE FAQ SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR creating Service FAQ: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateServiceFAQ(Long id, ServiceFAQRequestDTO dto, Long userId) {
        log.info("=== UPDATE SERVICE FAQ ID: {} ===", id);
        log.info("Update DTO: {}", dto);
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a category");
        }


        try {
            ServiceFAQ faq = serviceFAQRepository.findById(id)
                    .filter(f -> f.getDeleteStatus() == 2)
                    .orElseThrow(() -> {
                        log.error("Service FAQ not found or already deleted. faqId={}", id);
                        return new EntityNotFoundException("Service FAQ not found with ID: " + id);
                    });

            getAdminUser(userId); // validates admin

            Services service = serviceRepository.findById(dto.getServiceId())
                    .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                    .orElseThrow(() -> {
                        log.error("Service not found or not active. serviceId={}", dto.getServiceId());
                        return new EntityNotFoundException("Valid service not found with ID: " + dto.getServiceId());
                    });

            faq.setQuestion(dto.getQuestion());
            faq.setAnswer(dto.getAnswer());
            faq.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : faq.getDisplayOrder());
            faq.setService(service);
            faq.setActive(dto.isActive());
            faq.setDisplayStatus(dto.isDisplayStatus());

            ServiceFAQ updated = serviceFAQRepository.save(faq);
            log.info("Service FAQ updated successfully | ID: {}", updated.getId());

            Map<String, Object> response = mapToResponseMap(updated);
            log.info("=== UPDATE SERVICE FAQ SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR updating Service FAQ ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<Map<String, Object>> getServiceFAQById(Long id) {
        log.info("Fetching Service FAQ by ID: {}", id);
        return serviceFAQRepository.findById(id)
                .filter(f -> f.getDeleteStatus() == 2 && f.isActive() && f.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getServiceFAQByUuid(String uuid) {
        log.info("Fetching Service FAQ by UUID: {}", uuid);
        return serviceFAQRepository.findByUuid(uuid)
                .filter(f -> f.getDeleteStatus() == 2 && f.isActive() && f.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public List<Map<String, Object>> getServiceFAQsByServiceId(Long serviceId) {
        log.info("Fetching all FAQs for Service ID: {}", serviceId);
        return serviceFAQRepository.findByServiceId(serviceId)
                .stream()
                .filter(f -> f.getDeleteStatus() == 2 && f.isActive() && f.isDisplayStatus())
                .sorted(Comparator.comparingInt(ServiceFAQ::getDisplayOrder))
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void softDeleteServiceFAQ(Long id, Long userId) {
        log.info("=== SOFT DELETE SERVICE FAQ ID: {} by userId: {} ===", id, userId);

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required to create a category");
        }

        try {
            ServiceFAQ faq = serviceFAQRepository.findById(id)
                    .filter(f -> f.getDeleteStatus() == 2)
                    .orElseThrow(() -> {
                        log.error("Service FAQ not found. faqId={}", id);
                        return new EntityNotFoundException("Service FAQ not found with ID: " + id);
                    });

            getAdminUser(userId);

            faq.setDeleteStatus(1);
            faq.setActive(false);
            faq.setDisplayStatus(false);
            serviceFAQRepository.save(faq);

            log.warn("Service FAQ ID {} soft-deleted by user {}", id, userId);
            log.info("=== SOFT DELETE SERVICE FAQ SUCCESS ===");

        } catch (Exception e) {
            log.error("ERROR soft-deleting Service FAQ ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ====================== HELPER METHODS ======================

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

    // ====================== MAPPERS ======================

    private Map<String, Object> mapToResponseMap(ServiceFAQ faq) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", faq.getId());
        map.put("uuid", faq.getUuid());
        map.put("question", faq.getQuestion());
        map.put("answer", faq.getAnswer());
        map.put("displayOrder", faq.getDisplayOrder());
        map.put("serviceId", faq.getService().getId());
        map.put("active", faq.isActive());
        map.put("displayStatus", faq.isDisplayStatus());
        map.put("createdAt", faq.getCreatedAt());
        map.put("updatedAt", faq.getUpdatedAt());
        map.put("createdById", faq.getCreatedBy() != null ? faq.getCreatedBy().getId() : null);
        return map;
    }
}