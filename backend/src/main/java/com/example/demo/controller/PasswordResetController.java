/*package com.example.demo.controller;

import com.example.demo.dto.request.ForgotPasswordRequestDTO;
import com.example.demo.dto.request.ResetPasswordRequestDTO;
import com.example.demo.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // 1. Şifre sıfırlama talebi (email gönder)
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO requestDto) {
        passwordResetService.createPasswordResetToken(requestDto.getEmail());
        return ResponseEntity.ok().build();
    }

    // 2. Şifreyi sıfırla
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO updateDto) {
        System.out.println("DEBUG: Reset endpoint called");
        System.out.println("DEBUG: Token: " + updateDto.getToken());
        if (updateDto.getNewPassword() == null) {
            return ResponseEntity.badRequest().body("Yeni şifre boş olamaz");
        }
        System.out.println("DEBUG: New password length: " + updateDto.getNewPassword().length());

        boolean result = passwordResetService.resetPassword(updateDto.getToken(), updateDto.getNewPassword());
        System.out.println("DEBUG: Service result: " + result);

        if (result) {
            return ResponseEntity.ok().body("Şifre başarıyla sıfırlandı");
        } else {
            return ResponseEntity.badRequest().body("Token geçersiz veya süresi dolmuş.");
        }
    }
}*/