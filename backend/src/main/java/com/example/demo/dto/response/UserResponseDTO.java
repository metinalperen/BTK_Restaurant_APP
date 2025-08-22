package com.example.demo.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String photoBase64;
    private LocalDateTime createdAt;
    private Set<Integer> roles;
    private Boolean isActive;
}
