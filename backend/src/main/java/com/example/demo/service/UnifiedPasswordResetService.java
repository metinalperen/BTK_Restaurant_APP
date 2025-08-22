package com.example.demo.service;

import com.example.demo.exception.user.PasswordResetTokenInvalidException;
import com.example.demo.model.ResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ActivityLogService;
import com.example.demo.validation.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UnifiedPasswordResetService {

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResetTokenService resetTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Create password reset token and send email
     */
    public boolean createPasswordResetRequest(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Create token with 15 minutes expiration
        ResetToken resetToken = resetTokenService.createPasswordResetToken(user, 15);

        try {
            // Send email with reset link
            String resetLink = "http://localhost:5174/reset-password?token=" + resetToken.getToken();
            String subject = "Şifre Sıfırlama Talebi";
            String message = String.format(
                    "Merhaba %s,\n\n" +
                            "Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:\n\n%s\n\n" +
                            "Bu bağlantı 15 dakika boyunca geçerlidir.\n\n" +
                            "İyi günler dileriz.",
                    user.getName(), resetLink
            );

            emailService.sendEmail(user.getEmail(), subject, message);

            // Log the password reset request
            activityLogService.logActivity("PASSWORD_RESET_REQUESTED", "USER", user.getId(),
                    "Password reset requested for user: " + user.getEmail());

            return true;
        } catch (Exception e) {
            // If email fails, delete the token
            resetTokenService.deleteExistingTokens(user);
            throw new RuntimeException("Email gönderilemedi: " + e.getMessage());
        }
    }

    /**
     * Reset password using token
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<ResetToken> tokenOpt = resetTokenService.findByToken(token);
        if (tokenOpt.isEmpty()) {
            throw new PasswordResetTokenInvalidException("Token bulunamadı");
        }

        ResetToken resetToken = tokenOpt.get();

        // Check if token is valid
        if (!resetTokenService.isValid(resetToken)) {
            throw new PasswordResetTokenInvalidException("Token geçersiz veya süresi dolmuş");
        }

        if (!userValidator.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException("Şifre en az 6 karakter olmalı ve en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter (@$!%*?&) içermelidir");
        }
        User user = resetToken.getUser();

        // Update password
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);

        // Mark token as used
        resetTokenService.markAsUsed(resetToken);

        // Log the password reset
        activityLogService.logActivity("PASSWORD_RESET_COMPLETED", "USER", user.getId(),
                "Password reset completed for user: " + user.getEmail());

        return true;
    }

    /**
     * Validate token without resetting password
     */
    public boolean validateToken(String token) {
        Optional<ResetToken> tokenOpt = resetTokenService.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        ResetToken resetToken = tokenOpt.get();
        return resetTokenService.isValid(resetToken);
    }

    /**
     * Get user from token
     */
    public Optional<User> getUserFromToken(String token) {
        Optional<ResetToken> tokenOpt = resetTokenService.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        ResetToken resetToken = tokenOpt.get();
        if (!resetTokenService.isValid(resetToken)) {
            return Optional.empty();
        }

        return Optional.of(resetToken.getUser());
    }
}