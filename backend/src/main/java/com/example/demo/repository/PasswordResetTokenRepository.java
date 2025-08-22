/*package com.example.demo.repository;

import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    // Kullanıcıya göre token bulmak için eklenen metod
    Optional<PasswordResetToken> findByUser(User user);
}
*/

