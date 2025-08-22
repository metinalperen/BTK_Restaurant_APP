package com.example.demo.service;

import com.example.demo.model.ResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.ResetTokenRepository;
import com.example.demo.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ResetTokenService {

    @Autowired
    private ResetTokenRepository resetTokenRepository;

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Create a new reset token for password reset
     */
    public ResetToken createPasswordResetToken(User user, int minutes) {
        // Delete any existing unused tokens for this user
        deleteExistingTokens(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(minutes);
        ResetToken resetToken = new ResetToken(user, token, expiresAt, ResetToken.TokenType.PASSWORD_RESET);

        ResetToken savedToken = resetTokenRepository.save(resetToken);

        // Log token creation
        activityLogService.logActivity("TOKEN_CREATED", "RESET_TOKEN", savedToken.getId(),
                "Password reset token created for user: " + user.getEmail());

        return savedToken;
    }

    /**
     * Find token by string value
     */
    public Optional<ResetToken> findByToken(String token) {
        return resetTokenRepository.findByToken(token);
    }

    /**
     * Check if token is valid (not used and not expired)
     */
    public boolean isValid(ResetToken token) {
        return !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    /**
     * Mark token as used
     */
    public void markAsUsed(ResetToken token) {
        token.setUsed(true);
        resetTokenRepository.save(token);

        // Log token usage
        activityLogService.logActivity("TOKEN_USED", "RESET_TOKEN", token.getId(),
                "Password reset token used for user: " + token.getUser().getEmail());
    }

    /**
     * Delete existing unused tokens for a user
     */
    public void deleteExistingTokens(User user) {
        resetTokenRepository.deleteByUserId(user.getId());
    }

    /**
     * Delete expired tokens (cleanup method)
     */
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        // This would need a custom query in repository
        // resetTokenRepository.deleteByExpiresAtBefore(now);
    }

    /**
     * Get token by user (for cleanup purposes)
     */
    public Optional<ResetToken> findByUser(User user) {
        return resetTokenRepository.findByUserId(user.getId());
    }
}