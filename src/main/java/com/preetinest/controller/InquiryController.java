package com.preetinest.controller;

import com.preetinest.dto.request.InquiryRequestDTO;
import com.preetinest.service.InquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createInquiry(@RequestBody InquiryRequestDTO requestDTO) {
        Map<String, Object> response = inquiryService.createInquiry(requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInquiryById(@PathVariable Long id) {
        return inquiryService.getInquiryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Map<String, Object>> getInquiryByUuid(@PathVariable String uuid) {
        return inquiryService.getInquiryByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllActiveInquiries(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = inquiryService.getAllActiveInquiries(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateInquiry(@PathVariable Long id, @RequestBody InquiryRequestDTO requestDTO, @RequestParam Long userId) {
        Map<String, Object> response = inquiryService.updateInquiry(id, requestDTO, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteInquiry(@PathVariable Long id, @RequestParam Long userId) {
        inquiryService.softDeleteInquiry(id, userId);
        return ResponseEntity.noContent().build();
    }
}