package com.preetinest.impl;

import com.preetinest.config.S3Service;
import com.preetinest.dto.request.ClientRequestDTO;
import com.preetinest.dto.response.ClientResponseDTO;
import com.preetinest.entity.Clients;
import com.preetinest.entity.User;
import com.preetinest.repository.ClientRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.ClientService;
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
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Autowired
    private S3Service s3Service; // For root S3 upload

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> createClient(ClientRequestDTO requestDTO, Long userId) {
        User createdBy = getAdminUser(userId);

        Clients client = new Clients();
        client.setUuid(UUID.randomUUID().toString());
        client.setName(requestDTO.getName());
        client.setClientType(requestDTO.getClientType());
        client.setDescription(requestDTO.getDescription());
        client.setContactEmail(requestDTO.getContactEmail());
        client.setContactPhone(requestDTO.getContactPhone());
        client.setMetaTitle(requestDTO.getMetaTitle());
        client.setMetaKeyword(requestDTO.getMetaKeyword());
        client.setMetaDescription(requestDTO.getMetaDescription());
        client.setSlug(requestDTO.getSlug());
        client.setActive(requestDTO.isActive());
        client.setDisplayStatus(requestDTO.isDisplayStatus());
        client.setShowOnHome(requestDTO.isShowOnHome());
        client.setDeleteStatus(2);
        client.setCreatedBy(createdBy);

        // Upload logo directly to S3 root (no folder)
        if (requestDTO.getLogoBase64() != null && !requestDTO.getLogoBase64().isBlank()) {
            String fileName = s3Service.uploadBase64Image(requestDTO.getLogoBase64());
            client.setLogoUrl(fileName); // Only filename stored
        }

        Clients saved = clientRepository.save(client);
        return mapToResponseMap(saved);
    }

    @Override
    public Map<String, Object> updateClient(Long id, ClientRequestDTO requestDTO, Long userId) {
        Clients client = clientRepository.findById(id)
                .filter(c -> c.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        getAdminUser(userId);

        client.setName(requestDTO.getName());
        client.setClientType(requestDTO.getClientType());
        client.setDescription(requestDTO.getDescription());
        client.setContactEmail(requestDTO.getContactEmail());
        client.setContactPhone(requestDTO.getContactPhone());
        client.setMetaTitle(requestDTO.getMetaTitle());
        client.setMetaKeyword(requestDTO.getMetaKeyword());
        client.setMetaDescription(requestDTO.getMetaDescription());
        client.setSlug(requestDTO.getSlug());
        client.setActive(requestDTO.isActive());
        client.setDisplayStatus(requestDTO.isDisplayStatus());
        client.setShowOnHome(requestDTO.isShowOnHome());

        // Only update logo if new base64 is provided
        if (requestDTO.getLogoBase64() != null && !requestDTO.getLogoBase64().isBlank()) {
            String fileName = s3Service.uploadBase64Image(requestDTO.getLogoBase64());
            client.setLogoUrl(fileName);
        }

        Clients updated = clientRepository.save(client);
        return mapToResponseMap(updated);
    }

    @Override
    public Optional<Map<String, Object>> getClientById(Long id) {
        return clientRepository.findById(id)
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive() && c.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getClientByUuid(String uuid) {
        return clientRepository.findByUuid(uuid)
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive() && c.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public Optional<Map<String, Object>> getClientBySlug(String slug) {
        return clientRepository.findBySlug(slug)
                .filter(c -> c.getDeleteStatus() == 2 && c.isActive() && c.isDisplayStatus())
                .map(this::mapToResponseMap);
    }

    @Override
    public List<Map<String, Object>> getAllActiveClients() {
        return clientRepository.findAllActiveClients()
                .stream()
                .map(this::mapToResponseMap)
                .collect(Collectors.toList());
    }

    @Override
    public void softDeleteClient(Long id, Long userId) {
        Clients client = clientRepository.findById(id)
                .filter(c -> c.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        getAdminUser(userId);

        client.setDeleteStatus(1);
        client.setActive(false);
        client.setDisplayStatus(false);
        clientRepository.save(client);
    }

    // Helper: Validate admin
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

    // MAPPERS — WITH FULL S3 ROOT URL
    private ClientResponseDTO mapToResponseDTO(Clients client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setUuid(client.getUuid());
        dto.setName(client.getName());
        dto.setClientType(client.getClientType());
        dto.setDescription(client.getDescription());
        dto.setContactEmail(client.getContactEmail());
        dto.setContactPhone(client.getContactPhone());
        dto.setMetaTitle(client.getMetaTitle());
        dto.setMetaKeyword(client.getMetaKeyword());
        dto.setMetaDescription(client.getMetaDescription());
        dto.setSlug(client.getSlug());
        dto.setActive(client.isActive());
        dto.setDisplayStatus(client.isDisplayStatus());
        dto.setShowOnHome(client.isShowOnHome());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());
        dto.setCreatedById(client.getCreatedBy() != null ? client.getCreatedBy().getId() : null);

        // FULL S3 URL (root)
        dto.setLogoUrl(s3Service.getFullUrl(client.getLogoUrl()));

        return dto;
    }

    private Map<String, Object> mapToResponseMap(Clients client) {
        ClientResponseDTO dto = mapToResponseDTO(client);
        Map<String, Object> response = new HashMap<>();
        response.put("id", dto.getId());
        response.put("uuid", dto.getUuid());
        response.put("name", dto.getName());
        response.put("clientType", dto.getClientType());
        response.put("description", dto.getDescription());
        response.put("contactEmail", dto.getContactEmail());
        response.put("contactPhone", dto.getContactPhone());
        response.put("logoUrl", dto.getLogoUrl());     // https://preetinest.s3.../abc123.png
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
        return response;
    }
}