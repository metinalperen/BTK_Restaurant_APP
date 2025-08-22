package com.example.demo.dto.response;

/**
 * Login yanıtı için DTO
 */
public class LoginResponseDTO {
    private boolean success;
    private Long userId;
    private Long roleId;
    private String token;
    private String message;

    public LoginResponseDTO() {}

    public LoginResponseDTO(boolean success, Long userId, Long roleId, String token, String message) {
        this.success = success;
        this.userId = userId;
        this.roleId = roleId;
        this.token = token;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static LoginResponseDTO success(Long userId, Long roleId, String token) {
        return new LoginResponseDTO(true, userId, roleId, token, null);
    }

    public static LoginResponseDTO failure(String message) {
        return new LoginResponseDTO(false, null, null, null, message);
    }
} 