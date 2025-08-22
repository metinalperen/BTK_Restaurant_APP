package com.example.demo.dto.request;

import com.example.demo.enums.ReservationStatusConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {
    @Schema(description = "Masa ID'si", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Masa ID'si boş olamaz")
    @Positive(message = "Masa ID'si pozitif bir sayı olmalıdır")
    private Integer tableId;

    @Schema(description = "Müşteri adı", example = "Ahmet Yılmaz", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Müşteri adı boş olamaz")
    @Size(min = 2, max = 100, message = "Müşteri adı 2-100 karakter arasında olmalıdır")
    private String customerName;

    @Schema(description = "Müşteri telefon numarası", example = "5555555555", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Geçerli bir telefon numarası giriniz (10-11 rakam)")
    private String customerPhone;

    @Schema(description = "Rezervasyon tarihi", example = "2025-08-20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Rezervasyon tarihi boş olamaz")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @Schema(description = "Rezervasyon saati", example = "14:30", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Rezervasyon saati boş olamaz")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime reservationTime;

    @Schema(description = "Özel istekler", example = "Lütfen masa hazırlansın", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "Özel istekler 500 karakterden uzun olamaz")
    private String specialRequests;

    @Schema(description = "E-mail adresi", example = "musteri@email.com", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @Schema(description = "Kişi sayısı", example = "4", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "Kişi sayısı en az 1 olmalıdır")
    @Max(value = 20, message = "Kişi sayısı en fazla 20 olabilir")
    private Integer personCount;

    @Schema(description = "Rezervasyon durumu ID'si", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "Geçersiz durum ID'si")
    @Max(value = 5, message = "Geçersiz durum ID'si")
    private Integer statusId;

    @Schema(description = "Oluşturan Kullanıcı ID'si", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Oluşturan kullanıcı ID'si boş olamaz")
    @Positive(message = "Oluşturan kullanıcı ID'si pozitif bir sayı olmalıdır")
    private Integer createdBy;

    // Convenience methods for backward compatibility
    public java.time.LocalDate getReservationDate() {
        return reservationDate;
    }

    public java.time.LocalTime getReservationTime() {
        return reservationTime;
    }

    public String getReservationDateTimeString() {
        if (reservationDate == null || reservationTime == null) return null;
        return reservationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " + 
               reservationTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public void setReservationDate(LocalDate date) {
        this.reservationDate = date;
    }

    public void setReservationTime(LocalTime time) {
        this.reservationTime = time;
    }

    public void setReservationDateTime(String dateTimeString) {
        if (dateTimeString != null && !dateTimeString.trim().isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                this.reservationDate = dateTime.toLocalDate();
                this.reservationTime = dateTime.toLocalTime();
            } catch (Exception e) {
                throw new IllegalArgumentException("Geçersiz rezervasyon zamanı formatı: " + dateTimeString + ". Beklenen format: yyyy-MM-dd HH:mm");
            }
        }
    }

    public void setReservationDateTime(LocalDate date, LocalTime time) {
        this.reservationDate = date;
        this.reservationTime = time;
    }

    public String getFormattedCustomerName() {
        if (customerName == null || customerName.trim().isEmpty()) {
            return customerName;
        }
        return customerName.trim();
    }

    public String getFormattedPhoneNumber() {
        if (customerPhone == null || customerPhone.trim().isEmpty()) {
            return customerPhone;
        }
        // Sadece rakamları al ve 10-11 haneli yap
        String cleaned = customerPhone.replaceAll("[^0-9]", "");
        if (cleaned.length() == 10) {
            return cleaned;
        } else if (cleaned.length() == 11 && cleaned.startsWith("0")) {
            return cleaned.substring(1); // Başındaki 0'ı kaldır
        } else if (cleaned.length() == 11 && cleaned.startsWith("90")) {
            return cleaned.substring(2); // 90'ı kaldır
        }
        return cleaned;
    }

    /**
     * Status ID'nin geçerli olup olmadığını kontrol eder
     */
    public boolean isValidStatusId() {
        return ReservationStatusConstants.isValidStatusId(statusId);
    }

    /**
     * Status ID'ye karşılık gelen Türkçe ismi döndürür
     */
    public String getStatusNameInTurkish() {
        return ReservationStatusConstants.getStatusNameInTurkish(statusId);
    }

    /**
     * Status ID'ye karşılık gelen İngilizce ismi döndürür
     */
    public String getStatusName() {
        return ReservationStatusConstants.getStatusName(statusId);
    }
}
