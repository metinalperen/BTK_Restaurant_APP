package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.model.*;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(
        name = "Authorization Management",
        description = "APIs for managing user authentication and authorization (login, registration, health check)."
)
@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TemporaryPasswordService temporaryPasswordService;
    private final EmailService emailService;
    private final UnifiedPasswordResetService unifiedPasswordResetService;
    private final ActivityLogService activityLogService;

    @Autowired
    private ResetTokenService resetTokenService;
    private final UserMapper userMapper;
    @Autowired
    private UserRoleIdRepository userRoleIdRepository;

    @Validated
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account.")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        User savedUser = userService.registerUser(userRequestDTO);
        UserResponseDTO responseDTO = userMapper.toUserResponseDTO(savedUser);
        return ResponseEntity.ok(responseDTO);
    }

    @Validated
    @PostMapping("/login")
    @Operation(summary = "Login with credentials", description = "Authenticates a user and returns a comprehensive login response.")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Login isteği alındı: email={}", loginRequest.getEmail());
        LoginResponseDTO response = authService.login(loginRequest);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    // ✅ Unified forgot-password endpoint
    @Validated
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        try {
            boolean success = unifiedPasswordResetService.createPasswordResetRequest(request.getEmail());
            if (success) {
                return ResponseEntity.ok("Şifre sıfırlama maili gönderildi.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
            }
        } catch (Exception e) {
            log.error("Şifre sıfırlama hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Mail gönderilirken bir hata oluştu: " + e.getMessage());
        }
    }

    // ✅ Unified reset-password endpoint
    @Validated
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Yeni şifre boş olamaz");
        }

        boolean success = unifiedPasswordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (success) {
            return ResponseEntity.ok("Şifre başarıyla güncellendi.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz veya süresi dolmuş token");
        }
    }

    // ✅ New endpoint to validate token (for frontend validation)
    @Validated
    @PostMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Token gerekli");
        }

        boolean isValid = unifiedPasswordResetService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok("Token geçerli");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token geçersiz veya süresi dolmuş");
        }
    }

    @PostMapping("/bootstrap-admin")
    @Operation(summary = "Bootstrap first admin", description = "Creates the first admin with a temporary password sent via email. Only works when there are no users.")
    public ResponseEntity<?> bootstrapAdmin(@Valid @RequestBody BootstrapAdminRequestDTO req) {
        if (userRepository.count() > 1000) {
            // Log failed bootstrap attempt
            activityLogService.logBootstrapAdminSystemActivity(
                ActivityLogService.BOOTSTRAP_ADMIN_ATTEMPT, 
                "Bootstrap admin attempt rejected - system already has users"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bootstrap already completed");
        }

        String temporaryPassword = temporaryPasswordService.generateTemporaryPassword();

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user = userService.createUserWithTemporaryPassword(user, temporaryPassword);

        Role adminRole = roleRepository.findByName("admin").orElseGet(() -> {
            Role r = new Role();
            r.setName("admin");
            return roleRepository.save(r);
        });
        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserId(user.getId());
        userRoleId.setRoleId(adminRole.getId());

        UserRole userRole = new UserRole();
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(adminRole);
        user.getUserRoles().add(userRole);
        user = userRepository.save(user);

        // Log successful bootstrap admin creation
        activityLogService.logBootstrapAdminActivity(
            ActivityLogService.BOOTSTRAP_ADMIN_CREATED, 
            user.getId(),
            req.getName(),
            req.getEmail(),
            "First admin user created via bootstrap",
            "temporaryPasswordGenerated", "true"
        );

        try {
            String subject = "Admin Account Created - Temporary Password";
            String emailBody = String.format(
                    """
                            Hello %s,

                            Your admin account has been created successfully.

                            Email: %s
                            Temporary Password: %s

                            Please login with these credentials and change your password immediately.

                            Best regards,
                            System Administrator""",
                    req.getName(), req.getEmail(), temporaryPassword
            );

            emailService.sendEmail(req.getEmail(), subject, emailBody);

            // Log successful email delivery
            activityLogService.logBootstrapAdminActivity(
                ActivityLogService.BOOTSTRAP_ADMIN_EMAIL_SENT, 
                user.getId(),
                req.getName(),
                req.getEmail(),
                "Bootstrap admin welcome email sent successfully"
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin user created successfully. Temporary password has been sent to your email.");
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send email with temporary password: {}", e.getMessage());

            // Log email delivery failure
            activityLogService.logBootstrapAdminActivity(
                ActivityLogService.BOOTSTRAP_ADMIN_EMAIL_FAILED, 
                user.getId(),
                req.getName(),
                req.getEmail(),
                "Bootstrap admin welcome email delivery failed",
                "error", e.getMessage()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin user created but email delivery failed. Use the temporary password below.");
            response.put("email", user.getEmail());
            response.put("temporaryPassword", temporaryPassword);

            return ResponseEntity.ok(response);
        }
    }

    @Validated
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Allows authenticated users to change their password.")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> request) {

        try {
            String token = authorizationHeader.replace("Bearer ", "");
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            Long userId = authService.getUserIdFromToken(token);
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body("Current password and new password are required");
            }

            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            User user = userOpt.get();

            if (!userService.verifyPassword(currentPassword, user.getPasswordHash())) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }

            user.setPassword(newPassword);
            userService.updateUser(userId, user);

            return ResponseEntity.ok("Password changed successfully");

        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error changing password");
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks the health of the authentication service.")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service çalışıyor");
    }

    // user count for ease of use during frontend development
    // used in bootstrap admin determination: is user count > 0
    @GetMapping("/user-count")
    @Operation(summary = "Get user count", description = "Returns the total number of registered users.")
    public ResponseEntity<Long> getUserCount() {
        long count = userRepository.count();
        return ResponseEntity.ok(count);
    }

    // Endpoint to verify user role
    @GetMapping("/verify-role")
    @Operation(summary = "Verify user role", description = "Checks if the authenticated user has a specific role.")
    public ResponseEntity<RoleVerificationResponseDTO> verifyUserRole(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String roleName) {
        try {
            // Check if the authorization header is present and starts with "Bearer "
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                RoleVerificationResponseDTO response = new RoleVerificationResponseDTO();
                response.setAuthorized(false);
                response.setRoleName(roleName);
                response.setMessage("Authorization header is missing or invalid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            // Get the token
            String token = authorizationHeader.replace("Bearer ", "");

            // See if the token is valid
            if (!authService.validateToken(token)) {
                RoleVerificationResponseDTO response = new RoleVerificationResponseDTO();
                response.setAuthorized(false);
                response.setRoleName(roleName);
                response.setMessage("Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Long userId = authService.getUserIdFromToken(token);
            boolean hasRole = userService.userHasRole(userId, roleName);

            // Create response object
            RoleVerificationResponseDTO response = new RoleVerificationResponseDTO();
            response.setAuthorized(hasRole);
            response.setRoleName(roleName);
            try {
                Integer roleId = roleRepository.findByName(roleName)
                        .map(role -> Math.toIntExact(role.getId()))
                        .orElse(null);
                response.setRoleId(roleId);
            } catch (NullPointerException e) {
                log.error("Role ID not found for role: {}", roleName);
                response.setRoleId(null); // Set to null if role not found
            }
            response.setRedirectPath(determineRedirectPath(roleName));
            response.setMessage(hasRole ? "User has the required role" : "User does not have the required role");
            response.setExpiresAt(jwtUtil.extractExpiration(token).getTime()); // Token expiry timestamp
            return ResponseEntity.ok(response);


        } catch (IllegalArgumentException e) {
            // Handle case where token is malformed or user ID cannot be extracted
            log.error("Error extracting user ID from token: {}", e.getMessage());

            // Return a response indicating the error
            RoleVerificationResponseDTO response = new RoleVerificationResponseDTO();
            response.setAuthorized(false);
            response.setRoleName(roleName);
            response.setMessage("Invalid token format");
            response.setExpiresAt(System.currentTimeMillis() + 3600000); // Set expiry to 1 hour from now
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error verifying user role: {}", e.getMessage());
            // Return a generic error response
            RoleVerificationResponseDTO response = new RoleVerificationResponseDTO();
            response.setAuthorized(false);
            response.setRoleName(roleName);
            response.setMessage("An error occurred while verifying the user role");
            response.setExpiresAt(System.currentTimeMillis() + 3600000); // Set expiry

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String determineRedirectPath(String roleName) {
        switch (roleName.toLowerCase()) {
            case "admin": return "/admin/dashboard";
            case "garson": return "/garson/home";
            case "kasiyer": return "/kasiyer/home";
            default: return "/";
        }
    }
}
