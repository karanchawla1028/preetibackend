package com.preetinest.controller;

import com.preetinest.dto.request.BlogDetailRequestDTO;
import com.preetinest.service.BlogDetailService;
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
@RequestMapping("/api/blog-details")
@Tag(name = "Blog Detail Management", description = "APIs for managing blog details in PreetiNest Global Connect")
public class BlogDetailController {

    private final BlogDetailService blogDetailService;

    @Autowired
    public BlogDetailController(BlogDetailService blogDetailService) {
        this.blogDetailService = blogDetailService;
    }

    @PostMapping
    @Operation(summary = "Create a new blog detail", description = "Creates a new blog detail with the provided details; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog detail created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid blog detail data or userId"),
            @ApiResponse(responseCode = "404", description = "User or blog not found")
    })
    public ResponseEntity<Map<String, Object>> createBlogDetail(
            @Valid @RequestBody BlogDetailRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = blogDetailService.createBlogDetail(requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get blog detail by ID", description = "Retrieves a blog detail by its ID if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog detail found"),
            @ApiResponse(responseCode = "404", description = "Blog detail not found")
    })
    public ResponseEntity<Map<String, Object>> getBlogDetailById(@PathVariable Long id) {
        return blogDetailService.getBlogDetailById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get blog detail by UUID", description = "Retrieves a blog detail by its UUID if not deleted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog detail found"),
            @ApiResponse(responseCode = "404", description = "Blog detail not found")
    })
    public ResponseEntity<Map<String, Object>> getBlogDetailByUuid(@PathVariable String uuid) {
        return blogDetailService.getBlogDetailByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/blog/{blogId}")
    @Operation(summary = "Get blog details by blog ID", description = "Retrieves all active blog details for a given blog ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of blog details found"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<List<Map<String, Object>>> getBlogDetailsByBlogId(@PathVariable Long blogId) {
        return ResponseEntity.ok(blogDetailService.getBlogDetailsByBlogId(blogId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a blog detail", description = "Updates an existing blog detail by ID; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog detail updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog detail or user not found"),
            @ApiResponse(responseCode = "400", description = "Invalid blog detail data or userId")
    })
    public ResponseEntity<Map<String, Object>> updateBlogDetail(
            @PathVariable Long id,
            @Valid @RequestBody BlogDetailRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = blogDetailService.updateBlogDetail(id, requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a blog detail", description = "Marks a blog detail as deleted by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Blog detail deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Blog detail or user not found")
    })
    public ResponseEntity<Void> softDeleteBlogDetail(@PathVariable Long id, @RequestParam Long userId) {
        blogDetailService.softDeleteBlogDetail(id, userId);
        return ResponseEntity.noContent().build();
    }
}