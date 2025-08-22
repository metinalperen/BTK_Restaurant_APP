/*package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.service.ActivityLogService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ActivityLogService activityLogService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.activityLogService = activityLogService;
    }

    public void createPasswordResetToken(String email) {
        System.out.println("DEBUG: Creating password reset token for email: " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        System.out.println("DEBUG: User found: " + userOpt.isPresent());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("DEBUG: User ID: " + user.getId());

            Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUser(user);
            System.out.println("DEBUG: Existing token found: " + existingTokenOpt.isPresent());

            if (existingTokenOpt.isPresent()) {
                PasswordResetToken existingToken = existingTokenOpt.get();
                System.out.println("DEBUG: Existing token expiration: " + existingToken.getExpirationDate());

                if (existingToken.getExpirationDate().isAfter(LocalDateTime.now())) {
                    System.out.println("DEBUG: Using existing valid token");
                    String resetUrl = "http://localhost:3000/reset-password?token=" + existingToken.getToken();
                    emailService.sendEmail(email, "Şifre Sıfırlama",
                            "Şifrenizi sıfırlamak için linke tıklayın:\n" + resetUrl);
                    System.out.println("DEBUG: Email sent with existing token");
                    return;
                } else {
                    System.out.println("DEBUG: Deleting expired existing token");
                    tokenRepository.delete(existingToken);
                }
            }

            String token = UUID.randomUUID().toString();
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);
            System.out.println("DEBUG: Creating new token: " + token);
            System.out.println("DEBUG: Token expiration: " + expiration);

            PasswordResetToken resetToken = new PasswordResetToken(token, expiration, user);
            tokenRepository.save(resetToken);
            System.out.println("DEBUG: New token saved to database");

            String resetUrl = "http://localhost:3000/reset-password?token=" + token;
            emailService.sendEmail(email, "Şifre Sıfırlama",
                    "Şifrenizi sıfırlamak için linke tıklayın:\n" + resetUrl);
            System.out.println("DEBUG: Email sent with new token");
        } else {
            System.out.println("DEBUG: User not found for email: " + email);
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        System.out.println("DEBUG: Reset password called with token: " + token);
        System.out.println("DEBUG: New password length: " + newPassword.length());

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        System.out.println("DEBUG: Token found in database: " + tokenOpt.isPresent());

        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            System.out.println("DEBUG: Token expiration: " + resetToken.getExpirationDate());
            System.out.println("DEBUG: Current time: " + LocalDateTime.now());
            System.out.println("DEBUG: Token is valid: " + resetToken.getExpirationDate().isAfter(LocalDateTime.now()));

            if (resetToken.getExpirationDate().isAfter(LocalDateTime.now())) {
                User user = resetToken.getUser();
                System.out.println("DEBUG: User found: " + user.getEmail());
                System.out.println("DEBUG: User ID: " + user.getId());

                String encodedPassword = passwordEncoder.encode(newPassword);
                System.out.println("DEBUG: Password encoded successfully");
                System.out.println("DEBUG: Encoded password length: " + encodedPassword.length());

                user.setPasswordHash(encodedPassword);
                System.out.println("DEBUG: Password hash set to user object");

                userRepository.save(user);
                System.out.println("DEBUG: User saved to database successfully");
                activityLogService.logUserActivity(
                        user.getId(),
                        "PASSWORD_RESET",
                        "USER",
                        user.getId(),
                        "Password reset via reset token for user: " + user.getEmail()
                );

                tokenRepository.delete(resetToken);
                System.out.println("DEBUG: Reset token deleted from database");

                System.out.println("DEBUG: Password reset completed successfully");
                return true;
            } else {
                System.out.println("DEBUG: Token has expired");
            }
        } else {
            System.out.println("DEBUG: Token not found in database");
        }

        System.out.println("DEBUG: Password reset failed");
        return false;
    }
}*/