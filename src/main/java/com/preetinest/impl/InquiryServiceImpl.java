package com.preetinest.impl;

import com.preetinest.dto.request.InquiryRequestDTO;
import com.preetinest.dto.response.InquiryResponseDTO;
import com.preetinest.entity.Blog;
import com.preetinest.entity.Clients;
import com.preetinest.entity.Inquiry;
import com.preetinest.entity.Services;
import com.preetinest.entity.User;
import com.preetinest.repository.BlogRepository;
import com.preetinest.repository.ClientRepository;
import com.preetinest.repository.InquiryRepository;
import com.preetinest.repository.ServiceRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.InquiryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final ServiceRepository serviceRepository;
    private final BlogRepository blogRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Autowired
    public InquiryServiceImpl(InquiryRepository inquiryRepository, ServiceRepository serviceRepository,
                              BlogRepository blogRepository, ClientRepository clientRepository,
                              UserRepository userRepository) {
        this.inquiryRepository = inquiryRepository;
        this.serviceRepository = serviceRepository;
        this.blogRepository = blogRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> createInquiry(InquiryRequestDTO requestDTO) {
        String slug = requestDTO.getSlug();
        String pageType = "GENERAL";  // Default for homepage/contact form
        String pageName = "Homepage"; // or "Contact Us", "General Inquiry" – as per your preference

        // Only try to resolve slug if it's present and not blank
        if (slug != null && !slug.trim().isEmpty()) {
            // Check Service
            Optional<Services> serviceOpt = serviceRepository.findBySlug(slug);
            if (serviceOpt.isPresent()) {
                Services service = serviceOpt.get();
                if (service.getDeleteStatus() == 2 && service.isActive() && service.isDisplayStatus()) {
                    pageType = "SERVICE";
                    pageName = service.getName();
                }
            }

            // Check Blog if not already matched
            if ("GENERAL".equals(pageType)) {
                Optional<Blog> blogOpt = blogRepository.findBySlug(slug);
                if (blogOpt.isPresent()) {
                    Blog blog = blogOpt.get();
                    if (blog.getDeleteStatus() == 2 && blog.isActive() && blog.isDisplayStatus()) {
                        pageType = "BLOG";
                        pageName = blog.getTitle();
                    }
                }
            }

            // Check Client if still GENERAL
            if ("GENERAL".equals(pageType)) {
                Optional<Clients> clientOpt = clientRepository.findBySlug(slug);
                if (clientOpt.isPresent()) {
                    Clients client = clientOpt.get();
                    if (client.getDeleteStatus() == 2 && client.isActive() && client.isDisplayStatus()) {
                        pageType = "CLIENT";
                        pageName = client.getName();
                    }
                }
            }

            // If slug was provided but nothing matched → still allow, but keep as GENERAL with warning?
            // Or optionally throw error if you want strict validation only when slug is sent
            if ("GENERAL".equals(pageType) && slug != null && !slug.trim().isEmpty()) {
                // Option 1: Allow it as general (recommended for UX)
                pageName = "Unknown Page (Slug: " + slug + ")";

                // Option 2: Throw exception if slug is invalid (stricter)
                // throw new IllegalArgumentException("Invalid or inactive slug: " + slug);
            }
        }

        // Create inquiry
        Inquiry inquiry = new Inquiry();
        inquiry.setUuid(UUID.randomUUID().toString());
        inquiry.setName(requestDTO.getName());
        inquiry.setLocation(requestDTO.getLocation());
        inquiry.setMessage(requestDTO.getMessage());
        inquiry.setEmail(requestDTO.getEmail());
        inquiry.setPhone(requestDTO.getPhone());
        inquiry.setPageName(pageName);
        inquiry.setPageType(pageType);
        inquiry.setSlug(slug != null && !slug.trim().isEmpty() ? slug : null); // Save slug only if meaningful
        inquiry.setActive(true);
        inquiry.setDisplayStatus(true);
        inquiry.setDeleteStatus(2);

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return mapToResponseMap(savedInquiry);
    }
    @Override
    public Optional<Map<String, Object>> getInquiryById(Long id) {
        return inquiryRepository.findById(id)
                .filter(i -> i.getDeleteStatus() == 2 && i.isActive() && i.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getInquiryByUuid(String uuid) {
        return inquiryRepository.findByUuid(uuid)
                .filter(i -> i.getDeleteStatus() == 2 && i.isActive() && i.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Map<String, Object> getAllActiveInquiries(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN users can view inquiries");
        }


        Page<Inquiry> inquiryPage = inquiryRepository.findAllActiveInquiries(pageable);
        List<Map<String, Object>> inquiries = inquiryPage.getContent()
                .stream()
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("inquiries", inquiries);
        response.put("currentPage", inquiryPage.getNumber());
        response.put("totalItems", inquiryPage.getTotalElements());
        response.put("totalPages", inquiryPage.getTotalPages());
        response.put("pageSize", inquiryPage.getSize());

        return response;
    }

    @Override
    public Map<String, Object> updateInquiry(Long id, InquiryRequestDTO requestDTO, Long userId) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .filter(i -> i.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found with id: " + id));

        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN users can update inquiries");
        }

        // Update fields (slug/pageType/pageName not updated, assuming fixed)
        inquiry.setName(requestDTO.getName());
        inquiry.setLocation(requestDTO.getLocation());
        inquiry.setMessage(requestDTO.getMessage());
        inquiry.setEmail(requestDTO.getEmail());
        inquiry.setPhone(requestDTO.getPhone());
        inquiry.setActive(true); // or from request if needed
        inquiry.setDisplayStatus(true);
        inquiry.setCreatedBy(user);

        Inquiry updatedInquiry = inquiryRepository.save(inquiry);
        return mapToResponseMap(updatedInquiry);
    }

    @Override
    public void softDeleteInquiry(Long id, Long userId) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .filter(i -> i.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found with id: " + id));

        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN users can delete inquiries");
        }

        inquiry.setDeleteStatus(1);
        inquiry.setActive(false);
        inquiry.setDisplayStatus(false);
        inquiryRepository.save(inquiry);
    }

    private InquiryResponseDTO mapToResponseDTO(Inquiry inquiry) {
        InquiryResponseDTO dto = new InquiryResponseDTO();
        dto.setId(inquiry.getId());
        dto.setUuid(inquiry.getUuid());
        dto.setName(inquiry.getName());
        dto.setLocation(inquiry.getLocation());
        dto.setMessage(inquiry.getMessage());
        dto.setEmail(inquiry.getEmail());
        dto.setPhone(inquiry.getPhone());
        dto.setPageName(inquiry.getPageName());
        dto.setPageType(inquiry.getPageType());
        dto.setSlug(inquiry.getSlug());
        dto.setActive(inquiry.isActive());
        dto.setCreatedAt(inquiry.getCreatedAt());
        dto.setUpdatedAt(inquiry.getUpdatedAt());
        dto.setCreatedById(inquiry.getCreatedBy() != null ? inquiry.getCreatedBy().getId() : null);
        return dto;
    }

    private Map<String, Object> mapToResponseMap(Inquiry inquiry) {
        InquiryResponseDTO dto = mapToResponseDTO(inquiry);
        Map<String, Object> response = new HashMap<>();
        response.put("id", dto.getId());
        response.put("uuid", dto.getUuid());
        response.put("name", dto.getName());
        response.put("location", dto.getLocation());
        response.put("message", dto.getMessage());
        response.put("email", dto.getEmail());
        response.put("phone", dto.getPhone());
        response.put("pageName", dto.getPageName());
        response.put("pageType", dto.getPageType());
        response.put("slug", dto.getSlug());
        response.put("active", dto.isActive());
        response.put("createdAt", dto.getCreatedAt());
        response.put("updatedAt", dto.getUpdatedAt());
        response.put("createdById", dto.getCreatedById());
        return response;
    }
}