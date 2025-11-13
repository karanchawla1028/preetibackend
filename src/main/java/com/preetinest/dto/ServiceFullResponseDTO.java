package com.preetinest.dto;

import com.preetinest.dto.response.ServiceDetailResponseDTO;
import com.preetinest.dto.response.ServiceFAQResponseDTO;
import com.preetinest.dto.response.ServiceResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFullResponseDTO {
    private ServiceResponseDTO service;
    private List<ServiceDetailResponseDTO> details;
    private List<ServiceFAQResponseDTO> faqs;
}