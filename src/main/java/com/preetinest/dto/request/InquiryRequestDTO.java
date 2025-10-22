package com.preetinest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequestDTO {

    private String name;
    private String location;
    private String message;
    private String email;
    private String phone;
    private String slug;
}