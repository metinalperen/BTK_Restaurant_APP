package com.example.demo.dto.response;

import lombok.Data;

@Data
public class RoleVerificationResponseDTO {
    private boolean authorized;
    private String roleName;
    private Integer roleId;
    private String redirectPath;
    private String message;
    private Long expiresAt; // token expiry timestamp

    // constructors, getters, setters
}
