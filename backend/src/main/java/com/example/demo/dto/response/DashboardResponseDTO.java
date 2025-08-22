package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard yanıtı için DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {

    private long userId;
    private String userName;
    private String userEmail;
    private long roleId;
    private List<DiningTableResponseDto> tables;
    private String message;

    public static DashboardResponseDTO success(long userId, String userName, String userEmail,
                                               long roleId, List<DiningTableResponseDto> tables) {
        return DashboardResponseDTO.builder()
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .roleId(roleId)
                .tables(tables)
                .message("Dashboard verileri başarıyla getirildi")
                .build();
    }

    public static DashboardResponseDTO failure(String message) {
        return DashboardResponseDTO.builder()
                .message(message)
                .build();
    }
} 