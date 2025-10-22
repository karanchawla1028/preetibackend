package com.preetinest.impl;

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
    public ClientServiceImpl(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> createClient(ClientRequestDTO requestDTO, Long userId) {
        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can create clients");
            }
        }

        Clients client = new Clients();
        client.setUuid(UUID.randomUUID().toString());
        client.setName(requestDTO.getName());
        client.setClientType(requestDTO.getClientType());
        client.setDescription(requestDTO.getDescription());
        client.setContactEmail(requestDTO.getContactEmail());
        client.setContactPhone(requestDTO.getContactPhone());
        client.setLogoUrl(requestDTO.getLogoUrl());
        client.setMetaTitle(requestDTO.getMetaTitle());
        client.setMetaKeyword(requestDTO.getMetaKeyword());
        client.setMetaDescription(requestDTO.getMetaDescription());
        client.setSlug(requestDTO.getSlug());
        client.setActive(requestDTO.isActive());
        client.setDisplayStatus(requestDTO.isDisplayStatus());
        client.setShowOnHome(requestDTO.isShowOnHome());
        client.setDeleteStatus(2);
        client.setCreatedBy(createdBy);

        Clients savedClient = clientRepository.save(client);
        return mapToResponseMap(savedClient);
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
    public Map<String, Object> updateClient(Long id, ClientRequestDTO requestDTO, Long userId) {
        Clients client = clientRepository.findById(id)
                .filter(c -> c.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));

        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId)
                    .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            if (!"ADMIN".equalsIgnoreCase(createdBy.getRole().getName())) {
                throw new IllegalArgumentException("Only ADMIN users can update clients");
            }
        }

        client.setName(requestDTO.getName());
        client.setClientType(requestDTO.getClientType());
        client.setDescription(requestDTO.getDescription());
        client.setContactEmail(requestDTO.getContactEmail());
        client.setContactPhone(requestDTO.getContactPhone());
        client.setLogoUrl(requestDTO.getLogoUrl());
        client.setMetaTitle(requestDTO.getMetaTitle());
        client.setMetaKeyword(requestDTO.getMetaKeyword());
        client.setMetaDescription(requestDTO.getMetaDescription());
        client.setSlug(requestDTO.getSlug());
        client.setActive(requestDTO.isActive());
        client.setDisplayStatus(requestDTO.isDisplayStatus());
        client.setShowOnHome(requestDTO.isShowOnHome());
        client.setCreatedBy(createdBy);

        Clients updatedClient = clientRepository.save(client);
        return mapToResponseMap(updatedClient);
    }

    @Override
    public void softDeleteClient(Long id, Long userId) {
        Clients client = clientRepository.findById(id)
                .filter(c -> c.getDeleteStatus() == 2)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));

        User user = userRepository.findById(userId)
                .filter(u -> u.getDeleteStatus() == 2 && u.isEnable())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (!"ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("Only ADMIN users can delete clients");
        }

        client.setDeleteStatus(1);
        client.setActive(false);
        client.setDisplayStatus(false);
        clientRepository.save(client);
    }

    private ClientResponseDTO mapToResponseDTO(Clients client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setUuid(client.getUuid());
        dto.setName(client.getName());
        dto.setClientType(client.getClientType());
        dto.setDescription(client.getDescription());
        dto.setContactEmail(client.getContactEmail());
        dto.setContactPhone(client.getContactPhone());
        dto.setLogoUrl(client.getLogoUrl());
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
        response.put("logoUrl", dto.getLogoUrl());
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