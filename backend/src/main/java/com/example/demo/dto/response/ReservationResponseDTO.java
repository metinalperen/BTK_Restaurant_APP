package com.example.demo.dto.response;

import com.example.demo.enums.ReservationStatusConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Rezervasyon yanıtı için DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationResponseDTO {

    private Long id;
    
    private Long salonId;
    
    private String salonName;
    
    private Long tableId;
    
    private String customerName;
    
    private String customerPhone;
    
    private String specialRequests;
    
    private String email;
    
    private Integer personCount;
    
    private Integer statusId;
    
    private String statusName;
    
    private String statusNameInTurkish;
    
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Istanbul")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime reservationTime;


    public String getStatusInTurkish() {
        return ReservationStatusConstants.getStatusNameInTurkish(statusId);
    }


    public String getFormattedCreatedAt() {
        if (createdAt == null) return null;
        return createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public boolean hasSpecialRequests() {
        return specialRequests != null && !specialRequests.trim().isEmpty();
    }

    public String getReservationSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (customerName != null) {
            summary.append(customerName);
        }
        
        if (reservationDate != null && reservationTime != null) {
            if (summary.length() > 0) summary.append(" - ");
            summary.append(reservationDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            summary.append(" saat ").append(reservationTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        
        if (tableId != null) {
            summary.append(" (Masa ").append(tableId).append(")");
        }
        
        return summary.toString();
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
}