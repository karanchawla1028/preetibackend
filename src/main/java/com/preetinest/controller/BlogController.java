package com.preetinest.controller;

import com.preetinest.dto.request.BlogRequestDTO;
import com.preetinest.service.BlogService;
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
@RequestMapping("/api/blogs")
@Tag(name = "Blog Management", description = "APIs for managing blogs in PreetiNest Global Connect")
public class BlogController {

    private final BlogService blogService;

    @Autowired
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @PostMapping
    @Operation(summary = "Create a new blog", description = "Creates a new blog with the provided details; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid blog data or userId"),
            @ApiResponse(responseCode = "404", description = "User, category, subcategory, or service not found")
    })
    public ResponseEntity<Map<String, Object>> createBlog(
            @Valid @RequestBody BlogRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = blogService.createBlog(requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get blog by ID", description = "Retrieves a blog by its ID if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog found"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<Map<String, Object>> getBlogById(@PathVariable Long id) {
        return blogService.getBlogById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get blog by UUID", description = "Retrieves a blog by its UUID if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog found"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<Map<String, Object>> getBlogByUuid(@PathVariable String uuid) {
        return blogService.getBlogByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get blog by slug", description = "Retrieves a blog by its slug if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog found"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<Map<String, Object>> getBlogBySlug(@PathVariable String slug) {
        return blogService.getBlogBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all active blogs", description = "Retrieves a list of all active and non-deleted blogs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of active blogs")
    })
    public ResponseEntity<List<Map<String, Object>>> getAllActiveBlogs() {
        return ResponseEntity.ok(blogService.getAllActiveBlogs());
    }

    @GetMapping("/service/{serviceId}")
    @Operation(summary = "Get blogs by service ID", description = "Retrieves all active blogs for a given service ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of blogs found"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<List<Map<String, Object>>> getBlogsByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(blogService.getBlogsByServiceId(serviceId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a blog", description = "Updates an existing blog by ID; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog, user, category, subcategory, or service not found"),
            @ApiResponse(responseCode = "400", description = "Invalid blog data or userId")
    })
    public ResponseEntity<Map<String, Object>> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = blogService.updateBlog(id, requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a blog", description = "Marks a blog as deleted by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Blog deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Blog or user not found")
    })
    public ResponseEntity<Void> softDeleteBlog(@PathVariable Long id, @RequestParam Long userId) {
        blogService.softDeleteBlog(id, userId);
        return ResponseEntity.noContent().build();
    }
}