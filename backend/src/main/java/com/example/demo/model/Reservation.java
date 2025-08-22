package com.example.demo.model;

import com.example.demo.enums.ReservationStatusConstants;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Entity
@Table(name = "reservations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Müşteri adı boş olamaz")
    @Size(min = 2, max = 100, message = "Müşteri adı 2-100 karakter arasında olmalıdır")
    @Pattern(regexp = "^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$", message = "Müşteri adı sadece harf ve boşluk içerebilir")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotBlank(message = "Telefon numarası boş olamaz")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Geçerli bir telefon numarası giriniz (10-15 rakam)")
    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @NotNull(message = "Rezervasyon tarihi boş olamaz")
    @Column(name = "reservation_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @NotNull(message = "Rezervasyon saati boş olamaz")
    @Column(name = "reservation_time", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime reservationTime;

    @Size(max = 500, message = "Özel istekler 500 karakterden uzun olamaz")
    @Column(name = "special_requests")
    private String specialRequests;

    @Email(message = "Geçerli bir email adresi giriniz")
    @Column(name = "email")
    private String email;

    @Min(value = 1, message = "Kişi sayısı en az 1 olmalıdır")
    @Max(value = 20, message = "Kişi sayısı en fazla 20 olabilir")
    @Column(name = "person_count")
    private Integer personCount;

    @NotNull(message = "Rezervasyon durumu boş olamaz")
    @Column(name = "status_id", nullable = false)
    private Integer statusId;

    @NotNull(message = "Oluşturulma tarihi boş olamaz")
    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "email", "phone"})
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orders", "reservations"})
    private DiningTable table;

    // ==================== CUSTOM METHODS ====================

    public boolean isReservationTimeValid() {
        if (reservationTime == null) return false;
        try {
            LocalTime openTime = LocalTime.of(8, 0);
            LocalTime closeTime = LocalTime.of(23, 0);
            return !reservationTime.isBefore(openTime) && !reservationTime.isAfter(closeTime);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isReservationDateValid() {
        if (reservationDate == null) return false;
        try {
            LocalDate now = LocalDate.now();
            return !reservationDate.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }

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

    public void setReservationDate(java.time.LocalDate date) {
        this.reservationDate = date;
    }

    public void setReservationTime(java.time.LocalTime time) {
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

    public boolean isActiveReservation() {
        return statusId == ReservationStatusConstants.CONFIRMED;
    }

    public boolean isCancelledReservation() {
        return statusId == ReservationStatusConstants.CANCELLED;
    }

    public boolean isCompletedReservation() {
        return statusId == ReservationStatusConstants.COMPLETED;
    }

    public boolean isNoShowReservation() {
        return statusId == ReservationStatusConstants.NO_SHOW;
    }

    public boolean isPendingReservation() {
        return statusId == ReservationStatusConstants.PENDING;
    }

    public String getFormattedCustomerName() {
        if (customerName == null || customerName.trim().isEmpty()) return customerName;
        String[] words = customerName.trim().toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) formatted.append(" ");
            if (!words[i].isEmpty()) {
                formatted.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1));
            }
        }
        return formatted.toString();
    }

    public String getFormattedPhoneNumber() {
        if (customerPhone == null || customerPhone.trim().isEmpty()) return customerPhone;
        String cleaned = customerPhone.replaceAll("[^0-9+]", "");
        if (cleaned.startsWith("+")) return cleaned;
        else if (cleaned.startsWith("0")) return "+90" + cleaned.substring(1);
        else if (cleaned.length() == 10) return "+90" + cleaned;
        return cleaned;
    }

    public String getReservationSummary() {
        StringBuilder summary = new StringBuilder();
        if (customerName != null) summary.append(customerName);
        if (reservationDate != null && reservationTime != null) {
            if (!summary.isEmpty()) summary.append(" - ");
            summary.append(reservationDate)
                   .append(" saat ").append(reservationTime);
        }
        if (table != null) summary.append(" (Masa ").append(table.getId()).append(")");
        return summary.toString();
    }

    // Status helper methods
    public String getStatusName() {
        return ReservationStatusConstants.getStatusName(statusId);
    }

    public String getStatusNameInTurkish() {
        return ReservationStatusConstants.getStatusNameInTurkish(statusId);
    }
}
