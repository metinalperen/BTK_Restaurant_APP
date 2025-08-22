package com.example.demo.repository;

import com.example.demo.model.ResetToken;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    Optional<ResetToken> findByToken(String token);

    Optional<ResetToken> findByUserId(Long userId);

    Optional<ResetToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM ResetToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ResetToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Query("SELECT rt FROM ResetToken rt WHERE rt.user.id = :userId AND rt.tokenType = :tokenType AND rt.used = false")
    Optional<ResetToken> findActiveTokenByUserAndType(@Param("userId") Long userId, @Param("tokenType") String tokenType);
}