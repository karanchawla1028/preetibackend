// src/main/java/com/preetinest/impl/ClientServiceImpl.java

package com.preetinest.impl;

import com.preetinest.config.S3Service;
import com.preetinest.dto.request.ClientRequestDTO;
import com.preetinest.entity.Clients;
import com.preetinest.entity.User;
import com.preetinest.repository.ClientRepository;
import com.preetinest.repository.UserRepository;
import com.preetinest.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, UserRepository userRepository, S3Service s3Service) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    @Override
    public Map<String, Object> createClient(ClientRequestDTO dto, Long userId) {
        User createdBy = getAdminUser(userId);

        Clients client = new Clients();
        client.setUuid(UUID.randomUUID().toString());
        client.setName(dto.getName());
        client.setClientType(dto.getClientType());
        client.setDescription(dto.getDescription());
        client.setContactEmail(dto.getContactEmail());
        client.setContactPhone(dto.getContactPhone());
        client.setMetaTitle(dto.getMetaTitle());
        client.setMetaKeyword(dto.getMetaKeyword());
        client.setMetaDescription(dto.getMetaDescription());
        client.setSlug(dto.getSlug());
        client.setActive(dto.getActive());
        client.setDisplayStatus(dto.getDisplayStatus());
        client.setShowOnHome(dto.getShowOnHome());
        client.setDeleteStatus(2);
        client.setCreatedBy(createdBy);

        // Logo handled in controller → filename passed via dto.logo
        if (dto.getLogo() != null && !dto.getLogo().isBlank()) {
            String fullUrl = s3Service.getFullUrl(dto.getLogo());
            client.setLogo(dto.getLogo());
            client.setLogoUrl(fullUrl);
        }

        Clients saved = clientRepository.save(client);
        return mapToResponseMap(saved);
    }

    @Override
    public Map<String, Object> updateClient(Long id, ClientRequestDTO dto, Long userId) {
        Clients client = clientRepository.findById(id)
                .filter(c -> c.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        getAdminUser(userId);

        client.setName(dto.getName());
        client.setClientType(dto.getClientType());
        client.setDescription(dto.getDescription());
        client.setContactEmail(dto.getContactEmail());
        client.setContactPhone(dto.getContactPhone());
        client.setMetaTitle(dto.getMetaTitle());
        client.setMetaKeyword(dto.getMetaKeyword());
        client.setMetaDescription(dto.getMetaDescription());
        client.setSlug(dto.getSlug());
        client.setActive(dto.getActive());
        client.setDisplayStatus(dto.getDisplayStatus());
        client.setShowOnHome(dto.getShowOnHome());

        if (dto.getLogo() != null && !dto.getLogo().isBlank()) {
            String fullUrl = s3Service.getFullUrl(dto.getLogo());
            client.setLogo(dto.getLogo());
            client.setLogoUrl(fullUrl);
        }

        return mapToResponseMap(clientRepository.save(client));
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

    private User getAdminUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable() && "ADMIN".equalsIgnoreCase(u.getRole().getName()))
                .orElseThrow(() -> new IllegalArgumentException("Only ADMIN can perform this action"));
    }

    private Map<String, Object> mapToResponseMap(Clients client) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", client.getId());
        map.put("uuid", client.getUuid());
        map.put("name", client.getName());
        map.put("clientType", client.getClientType());
        map.put("description", client.getDescription());
        map.put("contactEmail", client.getContactEmail());
        map.put("contactPhone", client.getContactPhone());
        map.put("logo", client.getLogo());
        map.put("logoUrl", client.getLogoUrl() != null ? client.getLogoUrl() :
                (client.getLogo() != null ? s3Service.getFullUrl(client.getLogo()) : null));
        map.put("metaTitle", client.getMetaTitle());
        map.put("metaKeyword", client.getMetaKeyword());
        map.put("metaDescription", client.getMetaDescription());
        map.put("slug", client.getSlug());
        map.put("active", client.isActive());
        map.put("displayStatus", client.isDisplayStatus());
        map.put("showOnHome", client.isShowOnHome());
        map.put("createdAt", client.getCreatedAt());
        map.put("updatedAt", client.getUpdatedAt());
        map.put("createdById", client.getCreatedBy() != null ? client.getCreatedBy().getId() : null);
        return map;
    }
}