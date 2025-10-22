package com.preetinest.controller;

import com.preetinest.dto.request.ClientRequestDTO;
import com.preetinest.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Client Management", description = "APIs for managing clients in PreetiNest Global Connect")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @Operation(summary = "Create a new client", description = "Creates a new client with the provided details; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid client data or userId"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> createClient(
            @Valid @RequestBody ClientRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = clientService.createClient(requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID", description = "Retrieves a client by its ID if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Map<String, Object>> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get client by UUID", description = "Retrieves a client by its UUID if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Map<String, Object>> getClientByUuid(@PathVariable String uuid) {
        return clientService.getClientByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get client by slug", description = "Retrieves a client by its slug if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Map<String, Object>> getClientBySlug(@PathVariable String slug) {
        return clientService.getClientBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all active clients", description = "Retrieves a list of all active and non-deleted clients")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of active clients")
    })
    public ResponseEntity<List<Map<String, Object>>> getAllActiveClients() {
        return ResponseEntity.ok(clientService.getAllActiveClients());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a client", description = "Updates an existing client by ID; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated successfully"),
            @ApiResponse(responseCode = "404", description = "Client or user not found"),
            @ApiResponse(responseCode = "400", description = "Invalid client data or userId")
    })
    public ResponseEntity<Map<String, Object>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = clientService.updateClient(id, requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a client", description = "Marks a client as deleted by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Client or user not found")
    })
    public ResponseEntity<Void> softDeleteClient(@PathVariable Long id, @RequestParam Long userId) {
        clientService.softDeleteClient(id, userId);
        return ResponseEntity.noContent().build();
    }
}