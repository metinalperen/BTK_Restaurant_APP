package com.example.demo.controller;

import com.example.demo.dto.response.DashboardResponseDTO;
import com.example.demo.dto.response.DiningTableResponseDto;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.DiningTableService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(
        name = "Dashboard",
        description = "API for retrieving dashboard data including user information and dining tables."
)
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DashboardController {

    private final AuthService authService;
    private final UserService userService;
    private final DiningTableService diningTableService;
    private final ModelMapper modelMapper;

    @GetMapping
    @Operation(
            summary = "Get dashboard data",
            description = "Retrieves dashboard data for the authenticated user."
    )
    public ResponseEntity<DashboardResponseDTO> getDashboard(
            @Parameter(description = "Authorization header with JWT token", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        log.info("Dashboard isteği alındı");

        try {
            String token = authorizationHeader.replace("Bearer ", "");

            if (!authService.validateToken(token)) {
                log.error("Geçersiz JWT token");
                return ResponseEntity.badRequest()
                        .body(DashboardResponseDTO.failure("Geçersiz token"));
            }

            Long userId = authService.getUserIdFromToken(token);
            if (userId == null) {
                log.error("Token'dan kullanıcı ID'si alınamadı");
                return ResponseEntity.badRequest()
                        .body(DashboardResponseDTO.failure("Kullanıcı bilgisi alınamadı"));
            }

            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                log.error("Kullanıcı bulunamadı: userId={}", userId);
                return ResponseEntity.badRequest()
                        .body(DashboardResponseDTO.failure("Kullanıcı bulunamadı"));
            }

            User user = userOpt.get();

            if (!userService.isUserActive(userId)) {
                log.error("Kullanıcı aktif değil: userId={}", userId);
                return ResponseEntity.badRequest()
                        .body(DashboardResponseDTO.failure("Kullanıcı aktif değil"));
            }

            // ✔️ Yeni yapı: UserRole üzerinden rol ID'si
            long roleId = user.getUserRoles().isEmpty()
                    ? 0
                    : user.getUserRoles().iterator().next().getRole().getId();

            List<DiningTableResponseDto> tables = diningTableService.getAllTables().stream()
                    .map(table -> modelMapper.map(table, DiningTableResponseDto.class))
                    .collect(Collectors.toList());

            log.info("Dashboard verileri başarıyla getirildi: userId={}", userId);

            return ResponseEntity.ok(DashboardResponseDTO.success(
                    userId, user.getName(), user.getEmail(), roleId, tables));

        } catch (Exception e) {
            log.error("Dashboard verileri getirme hatası: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(DashboardResponseDTO.failure("Dashboard verileri getirilemedi: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Checks the health of the dashboard service."
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Dashboard service çalışıyor");
    }
}
