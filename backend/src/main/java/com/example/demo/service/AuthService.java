package com.example.demo.service;

import com.example.demo.dto.request.LoginRequestDTO;
import com.example.demo.dto.response.LoginResponseDTO;
import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import com.example.demo.exception.user.UserNotFoundException;
import com.example.demo.exception.user.UserInactiveException;
import com.example.demo.exception.user.InvalidCredentialsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Kullanıcı girişi yapılıyor: email={}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            Optional<User> userOpt = userService.getUserByEmail(loginRequest.getEmail());
            if (userOpt.isEmpty()) {
                log.error("Kullanıcı bulunamadı: email={}", loginRequest.getEmail());
                throw new UserNotFoundException();
            }

            User user = userOpt.get();

            if (!userService.isUserActive(user.getId())) {
                log.error("Kullanıcı aktif değil: userId={}", user.getId());
                throw new UserInactiveException();
            }

            String jwt = jwtUtil.generateToken(userDetails);

            Long roleId;
            if (user.getUserRoles().isEmpty()) {
                roleId = null;
            } else {
                String roleName = user.getUserRoles().iterator().next().getRole().getName();
                roleId = switch (roleName) {
                    case "admin" -> 0L;
                    case "waiter" -> 1L;
                    case "cashier" -> 2L;
                    default -> null;
                };
            }

            userService.logUserLogin(user.getId(), user.getEmail());

            log.info("Kullanıcı başarıyla giriş yaptı: userId={}, email={}", user.getId(), loginRequest.getEmail());

            return LoginResponseDTO.success(user.getId(), roleId, jwt);

        } catch (BadCredentialsException e) {
            log.error("Geçersiz kimlik bilgileri: email={}", loginRequest.getEmail());
            throw new InvalidCredentialsException();
        } catch (InvalidCredentialsException | UserInactiveException | UserNotFoundException ex) {
            throw ex; // propagate
        } catch (Exception e) {
            log.error("Login hatası: email={}, hata={}", loginRequest.getEmail(), e.getMessage());
            throw new InvalidCredentialsException();
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.getUserByEmail(email);
            return userOpt.map(User::getId).orElse(null);
        } catch (Exception e) {
            log.error("Token'dan kullanıcı ID'si çıkarılamadı: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean validateToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.getUserByEmail(email);
            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities(user.getUserRoles().stream()
                            .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName()))
                            .collect(Collectors.toList()))
                    .build();

            return jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            log.error("Token doğrulama hatası: {}", e.getMessage());
            return false;
        }
    }
}
