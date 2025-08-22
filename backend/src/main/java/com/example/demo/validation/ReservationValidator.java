package com.example.demo.validation;

import com.example.demo.enums.ReservationStatusConstants;
import com.example.demo.enums.TableStatusEnum;
import com.example.demo.model.DiningTable;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.model.RestaurantSettings;
import com.example.demo.repository.DiningTableRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.RestaurantSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReservationValidator {

    private final ReservationRepository reservationRepository;
    private final DiningTableRepository diningTableRepository;
    private final UserRepository userRepository;
    private final RestaurantSettingsService restaurantSettingsService;

    private static final int RESERVATION_DURATION_MINUTES = 120;

    @Autowired
    public ReservationValidator(ReservationRepository reservationRepository,
                                DiningTableRepository diningTableRepository,
                                UserRepository userRepository,
                                RestaurantSettingsService restaurantSettingsService) {
        this.reservationRepository = reservationRepository;
        this.diningTableRepository = diningTableRepository;
        this.userRepository = userRepository;
        this.restaurantSettingsService = restaurantSettingsService;
    }

    public void validateReservationCreation(Reservation reservation) {
        validateBasicFields(reservation);
        validateTimeConstraints(reservation);
        validateTableAvailability(reservation);
        validateUserExists(reservation);
        validateBusinessRules(reservation);
    }

    public void validateReservationUpdate(Reservation reservation) {
        validateBasicFields(reservation);
        validateTimeConstraints(reservation);
        validateTableAvailability(reservation);
        validateUserExists(reservation);
        validateBusinessRules(reservation);
    }

    public void validateReservationCancellation(Reservation reservation) {
        if (reservation.getStatusId() == ReservationStatusConstants.CANCELLED) {
            throw new IllegalArgumentException("Rezervasyon zaten iptal edilmiş");
        }

        if (reservation.getStatusId() == ReservationStatusConstants.COMPLETED) {
            throw new IllegalArgumentException("Tamamlanmış rezervasyon iptal edilemez");
        }

        if (isPastReservation(reservation)) {
            throw new IllegalArgumentException("Geçmiş rezervasyonlar iptal edilemez");
        }
    }

    private void validateBasicFields(Reservation reservation) {
        if (reservation.getCustomerName() == null || reservation.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Müşteri adı boş olamaz");
        }

        if (reservation.getCustomerPhone() == null || reservation.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Telefon numarası boş olamaz");
        }

        if (reservation.getReservationTime() == null) {
            throw new IllegalArgumentException("Rezervasyon saati boş olamaz");
        }

        if (reservation.getTable() == null) {
            throw new IllegalArgumentException("Masa belirtilmelidir");
        }

        // Salon validasyonu
        if (reservation.getTable().getSalon() == null) {
            throw new IllegalArgumentException("Masa bir salona ait olmalıdır");
        }

        if (reservation.getSpecialRequests() != null &&
                reservation.getSpecialRequests().length() > 500) {
            throw new IllegalArgumentException("Özel istekler 500 karakterden uzun olamaz");
        }
    }

    private void validateTimeConstraints(Reservation reservation) {
        try {
            System.out.println("=== DEBUG: Validating reservation date: " + reservation.getReservationDate() + " and time: " + reservation.getReservationTime());
            
            LocalDate reservationDate = reservation.getReservationDate();
            LocalTime reservationTime = reservation.getReservationTime();
            LocalDate today = LocalDate.now();

            if (reservationDate == null || reservationTime == null) {
                throw new IllegalArgumentException("Rezervasyon tarihi ve saati boş olamaz");
            }

            // Rezervasyon bugünden önceki bir tarih için yapılamaz
            if (reservationDate.isBefore(today)) {
                throw new IllegalArgumentException("Geçmiş tarihlerde rezervasyon yapılamaz");
            }

            // Bugün için geçmiş saatlerde rezervasyon yapılamaz
            if (reservationDate.isEqual(today) &&
                    reservationTime.isBefore(LocalTime.now())) {
                throw new IllegalArgumentException("Geçmiş saatlerde rezervasyon yapılamaz");
            }

            // Load current settings using the service (which handles fallback)
            RestaurantSettings settings = restaurantSettingsService.getSettingsEntity();

            LocalTime openTime = settings.getOpenTime();
            LocalTime closeTime = settings.getCloseTime();
            int cutoffMinutes = settings.getLastReservationCutoffMinutes() != null ? settings.getLastReservationCutoffMinutes() : 0;

            // Last reservation must be at least cutoff minutes before close
            LocalTime lastReservationTime = closeTime.minusMinutes(cutoffMinutes);

            if (reservationTime.isBefore(openTime) || reservationTime.isAfter(closeTime)) {
                throw new IllegalArgumentException("Rezervasyon saati " + openTime + " - " + closeTime + " arasında olmalıdır");
            }

            if (reservationTime.isAfter(lastReservationTime)) {
                throw new IllegalArgumentException("Son rezervasyon saati: " + lastReservationTime);
            }
        } catch (Exception e) {
            System.out.println("=== DEBUG: Validation error: " + e.getMessage());
            throw new IllegalArgumentException("Rezervasyon zamanı doğrulanamadı: " + e.getMessage());
        }
    }

    private void validateTableAvailability(Reservation reservation) {
        try {
            Long tableId = reservation.getTable().getId();
            LocalDate reservationDate = reservation.getReservationDate();
            LocalTime reservationTime = reservation.getReservationTime();

            if (reservationDate == null || reservationTime == null) {
                throw new IllegalArgumentException("Rezervasyon tarihi ve saati boş olamaz");
            }

            LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);

            DiningTable table = diningTableRepository.findById(tableId)
                    .orElseThrow(() -> new IllegalArgumentException("Masa bulunamadı: " + tableId));

            if (!"AVAILABLE".equals(table.getStatus().getName())) {
                throw new IllegalArgumentException("Masa müsait değil. Mevcut durum: " + table.getStatus().getName());
            }

            LocalDateTime reservationStart = reservationDateTime;
            LocalDateTime reservationEnd = reservationStart.plusMinutes(RESERVATION_DURATION_MINUTES);

            List<Reservation> overlappingReservations = reservationRepository
                    .findByTableAndStatusIdAndDateTimeBetween(
                            table,
                            ReservationStatusConstants.CONFIRMED,
                            reservationStart.toLocalDate(),
                            reservationStart.toLocalTime(),
                            reservationEnd.toLocalDate(),
                            reservationEnd.toLocalTime()
                    );

            if (!overlappingReservations.isEmpty()) {
                throw new IllegalArgumentException("Bu saatte zaten rezervasyon bulunmaktadır");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Rezervasyon zamanı doğrulanamadı: " + e.getMessage());
        }
    }

    private void validateUserExists(Reservation reservation) {
        if (reservation.getCreatedBy() == null) {
            throw new IllegalArgumentException("Oluşturan kullanıcı belirtilmelidir");
        }
        
        Long userId = reservation.getCreatedBy().getId();
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı: " + userId);
        }
    }

    private void validateBusinessRules(Reservation reservation) {
        try {
            // Maksimum rezervasyon sayısı kontrolü (günlük limit)
            LocalDate reservationDate = reservation.getReservationDate();
            LocalTime reservationTime = reservation.getReservationTime();
            
            if (reservationDate == null || reservationTime == null) {
                throw new IllegalArgumentException("Rezervasyon tarihi ve saati boş olamaz");
            }
            
            LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);
            LocalDateTime startOfDay = reservationDate.atStartOfDay();
            LocalDateTime endOfDay = reservationDate.atTime(23, 59, 59);
            
            List<Reservation> dailyReservations = reservationRepository
                    .findByReservationTimeBetween(reservationDate, LocalTime.MIN, reservationDate, LocalTime.MAX);
            
            // Eğer bu bir güncelleme ise, kendi rezervasyonunu hariç tut
            if (reservation.getId() != null) {
                dailyReservations = dailyReservations.stream()
                        .filter(r -> !r.getId().equals(reservation.getId()))
                        .toList();
            }
            
            // Günlük maksimum rezervasyon sayısı kontrolü (örnek: 50)
            int maxDailyReservations = 50;
            if (dailyReservations.size() >= maxDailyReservations) {
                throw new IllegalArgumentException("Günlük maksimum rezervasyon sayısına ulaşılmıştır");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Rezervasyon zamanı doğrulanamadı: " + e.getMessage());
        }
    }

    public boolean isPastReservation(Reservation reservation) {
        try {
            LocalDate reservationDate = reservation.getReservationDate();
            LocalTime reservationTime = reservation.getReservationTime();
            
            if (reservationDate == null || reservationTime == null) {
                return false;
            }
            
            LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);
            return reservationDateTime.isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTodayReservation(Reservation reservation) {
        try {
            LocalDate reservationDate = reservation.getReservationDate();
            if (reservationDate == null) {
                return false;
            }
            return reservationDate.isEqual(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isFutureReservation(Reservation reservation) {
        try {
            LocalDate reservationDate = reservation.getReservationDate();
            LocalTime reservationTime = reservation.getReservationTime();
            
            if (reservationDate == null || reservationTime == null) {
                return false;
            }
            
            LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);
            return reservationDateTime.isAfter(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isActiveReservation(Reservation reservation) {
        return reservation.getStatusId() == ReservationStatusConstants.CONFIRMED;
    }

    public boolean isCancelledReservation(Reservation reservation) {
        return reservation.getStatusId() == ReservationStatusConstants.CANCELLED;
    }

    public boolean isNoShowReservation(Reservation reservation) {
        return reservation.getStatusId() == ReservationStatusConstants.NO_SHOW;
    }

    public boolean isCompletedReservation(Reservation reservation) {
        return reservation.getStatusId() == ReservationStatusConstants.COMPLETED;
    }

    public boolean isPendingReservation(Reservation reservation) {
        return reservation.getStatusId() == ReservationStatusConstants.PENDING;
    }
}
