package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class UserRequestDTO {

    @Schema(description = "Kullanıcı Adı", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "İsim boş olamaz")
    @Size(min = 2, max = 50, message = "İsim 2-50 karakter arasında olmalıdır")
    @Pattern(regexp = "^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$", message = "İsim sadece harf içerebilir")
    private String name;

    @Schema(description = "Kullanıcı Email", example = "a@b.c", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz")
    private String email;

    @Schema(description = "Kullanıcı Şifresi", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, max = 100, message = "Şifre 6-100 karakter arasında olmalıdır")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Şifre en az bir küçük harf, bir büyük harf, bir rakam ve bir özel karakter içermelidir"
    )
    private String password;

    @Schema(description = "Telefon Numarası", example = "05551234567")
    @Size(max = 20, message = "Telefon numarası 20 karakterden uzun olamaz")
    private String phoneNumber;

    /* Bu kısım, profil fotoğrafını güncelleme için mevcut olan metodlar ve varsayılan fotoğraf kullanımı nedeni ile gereksiz kılındı.

    @Schema(description = "Kullanıcının profil fotoğrafı", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MultipartFile photo; // URL yerine artık direkt dosya
    */

    // createdAt alanı kaldırıldı - backend tarafından otomatik oluşturulacak

    @Schema(description = "Kullanıcı Rolü", example = "waiter", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Rol boş olamaz")
    @Pattern(regexp = "^(admin|waiter|cashier)$", message = "Geçerli bir rol seçiniz: admin, waiter, cashier")
    private String roleName;
}
