package com.preetinest.controller;

import com.preetinest.dto.request.BlogFAQRequestDTO;
import com.preetinest.service.BlogFAQService;
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
@RequestMapping("/api/blog-faqs")
@Tag(name = "Blog FAQ Management", description = "APIs for managing blog FAQs in PreetiNest Global Connect")
public class BlogFAQController {

    private final BlogFAQService blogFAQService;

    @Autowired
    public BlogFAQController(BlogFAQService blogFAQService) {
        this.blogFAQService = blogFAQService;
    }

    @PostMapping
    @Operation(summary = "Create a new blog FAQ", description = "Creates a new FAQ for a blog; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog FAQ created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid FAQ data or userId"),
            @ApiResponse(responseCode = "404", description = "User or blog not found")
    })
    public ResponseEntity<Map<String, Object>> createBlogFAQ(
            @Valid @RequestBody BlogFAQRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = blogFAQService.createBlogFAQ(requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get blog FAQ by ID", description = "Retrieves a blog FAQ by its ID if not deleted, active, and displayed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog FAQ found"),
            @ApiResponse(responseCode = "404", description = "Blog FAQ not found")
    })
    public ResponseEntity<Map<String, Object>> getBlogFAQById(@PathVariable Long id) {
        return blogFAQService.getBlogFAQById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Get blog FAQ by UUID", description = "Retrieves a blog FAQ by its UUID if not deleted, active, and displayed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog FAQ found"),
            @ApiResponse(responseCode = "404", description = "Blog FAQ not found")
    })
    public ResponseEntity<Map<String, Object>> getBlogFAQByUuid(@PathVariable String uuid) {
        return blogFAQService.getBlogFAQByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/blog/{blogId}")
    @Operation(summary = "Get blog FAQs by blog ID", description = "Retrieves all active, non-deleted, and displayed FAQs for a given blog ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of blog FAQs found"),
            @ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<List<Map<String, Object>>> getBlogFAQsByBlogId(@PathVariable Long blogId) {
        return ResponseEntity.ok(blogFAQService.getBlogFAQsByBlogId(blogId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a blog FAQ", description = "Updates an existing blog FAQ by ID; userId is optional for ADMIN role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog FAQ updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog FAQ or user not found"),
            @ApiResponse(responseCode = "400", description = "Invalid FAQ data or userId")
    })
    public ResponseEntity<Map<String, Object>> updateBlogFAQ(
            @PathVariable Long id,
            @Valid @RequestBody BlogFAQRequestDTO requestDTO,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = blogFAQService.updateBlogFAQ(id, requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a blog FAQ", description = "Marks a blog FAQ as deleted by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Blog FAQ deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Blog FAQ or user not found")
    })
    public ResponseEntity<Void> softDeleteBlogFAQ(@PathVariable Long id, @RequestParam Long userId) {
        blogFAQService.softDeleteBlogFAQ(id, userId);
        return ResponseEntity.noContent().build();
    }
}
