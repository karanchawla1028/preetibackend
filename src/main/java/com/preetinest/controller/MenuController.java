package com.preetinest.controller;

import com.preetinest.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@Tag(name = "Menu Management", description = "API for fetching menu data in PreetiNest Global Connect")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/fetch")
    @Operation(summary = "Fetch all menu items", description = "Retrieves active services, blogs, clients, and who we are details for menu display")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu items fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No menu items found")
    })
    public ResponseEntity<Map<String, Object>> fetchAllMenuItems() {
        Map<String, Object> response = menuService.fetchAllMenuItems();
        return ResponseEntity.ok(response);
    }
}