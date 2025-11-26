package com.preetinest.impl;

import com.preetinest.config.S3Service;
import com.preetinest.dto.*;
import com.preetinest.dto.response.*;
import com.preetinest.entity.*;
import com.preetinest.repository.*;
import com.preetinest.service.ServiceService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceServiceImpl implements ServiceService {

    // ← FIXED: Logger now works!
    private static final Logger log = LoggerFactory.getLogger(ServiceServiceImpl.class);

    private final ServiceRepository serviceRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final UserRepository userRepository;
    private final ServiceDetailRepository serviceDetailRepository;
    private final ServiceFAQRepository serviceFAQRepository;
    @Autowired
    private S3Service s3Service;

    @Autowired
    public ServiceServiceImpl(
            ServiceRepository serviceRepository,
            SubCategoryRepository subCategoryRepository,
            UserRepository userRepository,
            ServiceDetailRepository serviceDetailRepository,
            ServiceFAQRepository serviceFAQRepository) {
        this.serviceRepository = serviceRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.userRepository = userRepository;
        this.serviceDetailRepository = serviceDetailRepository;
        this.serviceFAQRepository = serviceFAQRepository;
    }

    @Override
    public Map<String, Object> createService(ServiceRequestDTO requestDTO, Long userId) {
        log.info("=== CREATE SERVICE START ===");
        log.info("Request DTO: {}", requestDTO);
        log.info("Created by userId: {}", userId);

        try {
            validateSlug(requestDTO.getSlug(), null);
            User createdBy = getAdminUser(userId);

            SubCategory subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                    .filter(sc -> sc.getDeleteStatus() == 2)
                    .orElseThrow(() -> new EntityNotFoundException("Subcategory not found with ID: " + requestDTO.getSubCategoryId()));

            Services service = new Services();
            service.setUuid(UUID.randomUUID().toString());
            service.setName(requestDTO.getName());
            service.setDescription(requestDTO.getDescription());
            service.setSubCategory(subCategory);
            service.setMetaTitle(requestDTO.getMetaTitle());
            service.setMetaKeyword(requestDTO.getMetaKeyword());
            service.setMetaDescription(requestDTO.getMetaDescription());
            service.setSlug(requestDTO.getSlug());
            service.setActive(requestDTO.isActive());
            service.setDisplayStatus(requestDTO.isDisplayStatus());
            service.setShowOnHome(requestDTO.isShowOnHome());
            service.setDeleteStatus(2);
            service.setCreatedBy(createdBy);

            // FIXED & LOGGED
            service.setIconUrl(requestDTO.getIconUrl());
            service.setImage(requestDTO.getImage());
            log.info("IconUrl saved: {}", requestDTO.getIconUrl());
            log.info("Image saved: {}", requestDTO.getImage());

            Services saved = serviceRepository.save(service);
            log.info("Service created successfully with ID: {} and UUID: {}", saved.getId(), saved.getUuid());

            Map<String, Object> response = mapToResponseMap(saved);
            log.info("=== CREATE SERVICE SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR creating service: {}", e.getMessage(), e);
            throw e; // re-throw so controller returns 400/500
        }
    }
    @Override
    public Map<String, Object> updateService(Long id, ServiceRequestDTO requestDTO, Long userId) {
        log.info("=== UPDATE SERVICE ID: {} ===", id);
        log.info("Update DTO: {}", requestDTO);

        try {
            Services service = serviceRepository.findById(id)
                    .filter(s -> s.getDeleteStatus() == 2)
                    .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + id));

            validateSlug(requestDTO.getSlug(), id);
            getAdminUser(userId);

            SubCategory subCategory = subCategoryRepository.findById(requestDTO.getSubCategoryId())
                    .filter(sc -> sc.getDeleteStatus() == 2)
                    .orElseThrow(() -> new EntityNotFoundException("Subcategory not found with ID: " + requestDTO.getSubCategoryId()));

            service.setName(requestDTO.getName());
            service.setDescription(requestDTO.getDescription());
            service.setSubCategory(subCategory);
            service.setMetaTitle(requestDTO.getMetaTitle());
            service.setMetaKeyword(requestDTO.getMetaKeyword());
            service.setMetaDescription(requestDTO.getMetaDescription());
            service.setSlug(requestDTO.getSlug());
            service.setActive(requestDTO.isActive());
            service.setDisplayStatus(requestDTO.isDisplayStatus());
            service.setShowOnHome(requestDTO.isShowOnHome());

            service.setIconUrl(requestDTO.getIconUrl());
            service.setImage(requestDTO.getImage());
            log.info("Updated IconUrl: {}", requestDTO.getIconUrl());
            log.info("Updated Image: {}", requestDTO.getImage());

            Services updated = serviceRepository.save(service);
            log.info("Service updated successfully | ID: {}", updated.getId());

            Map<String, Object> response = mapToResponseMap(updated);
            log.info("=== UPDATE SERVICE SUCCESS ===");
            return response;

        } catch (Exception e) {
            log.error("ERROR updating service ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    @Override
    public Optional<Map<String, Object>> getServiceById(Long id) {
        return serviceRepository.findById(id)
                .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getServiceByUuid(String uuid) {
        log.info("Fetching service by UUID: {}", uuid);
        return serviceRepository.findByUuid(uuid)
                .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getServiceBySlug(String slug) {
        log.info("Fetching service by slug: {}", slug);
        return serviceRepository.findBySlug(slug)
                .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus())
                .map(service -> {
                    log.info("Found service: {} | IconUrl: {} | Image: {}", service.getName(), service.getIconUrl(), service.getImage());
                    return mapToResponseMap(service);
                });
    }

    @Override
    public List<Map<String, Object>> getAllActiveServices() {
        return serviceRepository.findAllActiveServices()
                .stream()
                .filter(s -> s.isActive() && s.isDisplayStatus())
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    // FIXED: This method was missing → now implemented
    @Override
    public Optional<Map<String, Object>> getServiceWithDetailsById(Long id) {
        Optional<Services> serviceOpt = serviceRepository.findById(id)
                .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus());

        if (serviceOpt.isEmpty()) {
            return Optional.empty();
        }

        Services service = serviceOpt.get();

        List<ServiceDetailResponseDTO> details = serviceDetailRepository.findByServiceId(id)
                .stream()
                .filter(d -> d.getDeleteStatus() == 2 && d.isActive() && d.isDisplayStatus())
                .map(this::mapToServiceDetailResponseDTO)
                .collect(Collectors.toList());

        ServiceResponseDTO dto = mapToServiceResponseDTO(service);
        dto.setServiceDetails(details);

        Map<String, Object> response = new HashMap<>();
        response.put("id", dto.getId());
        response.put("uuid", dto.getUuid());
        response.put("name", dto.getName());
        response.put("description", dto.getDescription());
        response.put("subCategoryId", dto.getSubCategoryId());
        response.put("subCategoryName", dto.getSubCategoryName());
        response.put("categoryId", dto.getCategoryId());
        response.put("categoryName", dto.getCategoryName());
        response.put("iconUrl", dto.getIconUrl());
        response.put("image", dto.getImage());
        response.put("metaTitle", dto.getMetaTitle());
        response.put("metaKeyword", dto.getMetaKeyword());
        response.put("metaDescription", dto.getMetaDescription());
        response.put("slug", dto.getSlug());
        response.put("active", dto.isActive());
        response.put("displayStatus", dto.isDisplayStatus());
        response.put("showOnHome", dto.isShowOnHome());
        response.put("createdAt", dto.getCreatedAt());
        response.put("updatedAt", dto.getUpdatedAt());
        response.put("createdById", dto.getCreatedById());
        response.put("serviceDetails", dto.getServiceDetails());

        return Optional.of(response);
    }

    @Override
    public Optional<ServiceFullResponseDTO> getFullServiceBySlug(String slug) {
        Optional<Services> opt = serviceRepository.findBySlug(slug)
                .filter(s -> s.getDeleteStatus() == 2 && s.isActive() && s.isDisplayStatus());
        if (opt.isEmpty()) return Optional.empty();

        Services service = opt.get();
        ServiceResponseDTO dto = mapToServiceResponseDTO(service);

        List<ServiceDetailResponseDTO> details = serviceDetailRepository.findByServiceId(service.getId())
                .stream()
                .filter(d -> d.getDeleteStatus() == 2 && d.isActive() && d.isDisplayStatus())
                .sorted(Comparator.comparingInt(ServiceDetail::getDisplayOrder))
                .map(this::mapToServiceDetailResponseDTO)
                .collect(Collectors.toList());

        List<ServiceFAQResponseDTO> faqs = serviceFAQRepository.findByServiceId(service.getId())
                .stream()
                .filter(f -> f.getDeleteStatus() == 2 && f.isActive() && f.isDisplayStatus())
                .sorted(Comparator.comparingInt(ServiceFAQ::getDisplayOrder))
                .map(this::mapToServiceFAQResponseDTO)
                .collect(Collectors.toList());

        dto.setServiceDetails(details);

        ServiceFullResponseDTO response = new ServiceFullResponseDTO();
        response.setService(dto);
        response.setDetails(details);
        response.setFaqs(faqs);
        return Optional.of(response);
    }

    @Override
    public void softDeleteService(Long id, Long userId) {
        Services service = serviceRepository.findById(id)
                .filter(s -> s.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        getAdminUser(userId);
        service.setDeleteStatus(1);
        service.setActive(false);
        service.setDisplayStatus(false);
        service.setShowOnHome(false);
        serviceRepository.save(service);
    }

    // ====================== HELPERS ======================

    private User getAdminUser(Long userId) {
        if (userId == null) {
            log.warn("userId is null");
            return null;
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
        serviceRepository.findBySlug(slug).ifPresent(existing -> {
            if (existing.getDeleteStatus() == 2 && (excludeId == null || !existing.getId().equals(excludeId))) {
                log.error("Duplicate slug detected: '{}' (existing ID: {})", slug, existing.getId());
                throw new IllegalArgumentException("Slug '" + slug + "' already exists");
            }
        });
    }

    // ====================== MAPPERS (FULL S3 URLS) ======================

    private ServiceResponseDTO mapToServiceResponseDTO(Services service) {
        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(service.getId());
        dto.setUuid(service.getUuid());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setSubCategoryId(service.getSubCategory().getId());
        dto.setSubCategoryName(service.getSubCategory().getName());
        dto.setCategoryId(service.getSubCategory().getCategory().getId());
        dto.setCategoryName(service.getSubCategory().getCategory().getName());

        dto.setIconUrl(s3Service.getFullUrl(service.getIconUrl()));
        dto.setImage(s3Service.getFullUrl(service.getImage()));

        dto.setMetaTitle(service.getMetaTitle());
        dto.setMetaKeyword(service.getMetaKeyword());
        dto.setMetaDescription(service.getMetaDescription());
        dto.setSlug(service.getSlug());
        dto.setActive(service.isActive());
        dto.setDisplayStatus(service.isDisplayStatus());
        dto.setShowOnHome(service.isShowOnHome());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        dto.setCreatedById(service.getCreatedBy() != null ? service.getCreatedBy().getId() : null);
        return dto;
    }

    private Map<String, Object> mapToResponseMap(Services service) {
        ServiceResponseDTO dto = mapToServiceResponseDTO(service);
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("uuid", dto.getUuid());
        map.put("name", dto.getName());
        map.put("description", dto.getDescription());
        map.put("subCategoryId", dto.getSubCategoryId());
        map.put("subCategoryName", dto.getSubCategoryName());
        map.put("categoryId", dto.getCategoryId());
        map.put("categoryName", dto.getCategoryName());
        map.put("iconUrl", dto.getIconUrl());
        map.put("image", dto.getImage());
        map.put("metaTitle", dto.getMetaTitle());
        map.put("metaKeyword", dto.getMetaKeyword());
        map.put("metaDescription", dto.getMetaDescription());
        map.put("slug", dto.getSlug());
        map.put("active", dto.isActive());
        map.put("displayStatus", dto.isDisplayStatus());
        map.put("showOnHome", dto.isShowOnHome());
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedAt", dto.getUpdatedAt());
        map.put("createdById", dto.getCreatedById());
        return map;
    }

    private ServiceDetailResponseDTO mapToServiceDetailResponseDTO(ServiceDetail detail) {
        ServiceDetailResponseDTO dto = new ServiceDetailResponseDTO();
        dto.setId(detail.getId());
        dto.setUuid(detail.getUuid());
        dto.setHeading(detail.getHeading());
        dto.setDetails(detail.getDetails());
        dto.setDisplayOrder(detail.getDisplayOrder());
        dto.setServiceId(detail.getService().getId());
        dto.setActive(detail.isActive());
        dto.setCreatedAt(detail.getCreatedAt());
        dto.setUpdatedAt(detail.getUpdatedAt());
        dto.setCreatedById(detail.getCreatedBy() != null ? detail.getCreatedBy().getId() : null);
        return dto;
    }

    private ServiceFAQResponseDTO mapToServiceFAQResponseDTO(ServiceFAQ faq) {
        ServiceFAQResponseDTO dto = new ServiceFAQResponseDTO();
        dto.setId(faq.getId());
        dto.setUuid(faq.getUuid());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());
        dto.setDisplayOrder(faq.getDisplayOrder());
        dto.setServiceId(faq.getService().getId());
        dto.setActive(faq.isActive());
        dto.setDisplayStatus(faq.isDisplayStatus());
        dto.setCreatedAt(faq.getCreatedAt());
        dto.setUpdatedAt(faq.getUpdatedAt());
        dto.setCreatedById(faq.getCreatedBy() != null ? faq.getCreatedBy().getId() : null);
        return dto;
    }
}