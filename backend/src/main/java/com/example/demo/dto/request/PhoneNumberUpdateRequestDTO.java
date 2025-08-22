package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for updating user phone numbers
 * Includes validation for phone number format
 */
@Data
public class PhoneNumberUpdateRequestDTO {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$",
            message = "Phone number must be between 10-20 characters and contain only digits, spaces, hyphens, parentheses, and optionally a plus sign")
    private String phoneNumber;
}