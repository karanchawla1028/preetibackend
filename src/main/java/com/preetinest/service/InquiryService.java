package com.preetinest.service;

import com.preetinest.dto.request.InquiryRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InquiryService {

    Map<String, Object> createInquiry(InquiryRequestDTO requestDTO);

    Optional<Map<String, Object>> getInquiryById(Long id);

    Optional<Map<String, Object>> getInquiryByUuid(String uuid);

    Map<String, Object> getAllActiveInquiries(Long userId, Pageable pageable);

    Map<String, Object> updateInquiry(Long id, InquiryRequestDTO requestDTO, Long userId);

    void softDeleteInquiry(Long id, Long userId);
}