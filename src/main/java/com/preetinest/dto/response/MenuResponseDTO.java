package com.preetinest.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MenuResponseDTO {
    private List<Map<String, Object>> services;
    private List<Map<String, Object>> blogs;
    private List<Map<String, Object>> clients;
    private List<Map<String, Object>> whoWeAre;
}