package com.preetinest.service;

import com.preetinest.dto.request.ClientRequestDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ClientService {
    Optional<Map<String, Object>> getClientById(Long id);
    Optional<Map<String, Object>> getClientByUuid(String uuid);
    Optional<Map<String, Object>> getClientBySlug(String slug);
    List<Map<String, Object>> getAllActiveClients();
    void softDeleteClient(Long id, Long userId);
    Map<String, Object> createClient(ClientRequestDTO requestDTO, Long userId);
    Map<String, Object> updateClient(Long id, ClientRequestDTO requestDTO, Long userId);
}