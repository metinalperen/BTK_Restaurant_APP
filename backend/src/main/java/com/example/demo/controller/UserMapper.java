package com.example.demo.controller;

import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.User;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getPhotoBlob() != null && user.getPhotoBlob().length > 0) {
            dto.setPhotoBase64(Base64.getEncoder().encodeToString(user.getPhotoBlob()));
        }

        if (user.getUserRoles() != null) {
            dto.setRoles(user.getUserRoles().stream()
                    .map(userRole -> switch (userRole.getRole().getName()) {
                        case "admin" -> 0;
                        case "waiter" -> 1;
                        case "cashier" -> 2;
                        default -> -1;
                    })
                    .collect(Collectors.toSet()));
        }

        return dto;
    }
}

