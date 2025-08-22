package com.example.demo.service;

import com.example.demo.enums.ReservationStatusConstants;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.diningtable.TableNotFoundException;
import com.example.demo.exception.reservation.ReservationConflictException;
import com.example.demo.exception.reservation.ReservationNotFoundException;
import com.example.demo.exception.reservation.ReservationValidationException;
import com.example.demo.model.DiningTable;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.repository.DiningTableRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DiningTableService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationService {

    private static final int CONFLICT_WINDOW_MINUTES = 120; // +/- 2 saat

    private final ReservationRepository reservationRepository;
    private final DiningTableRepository diningTableRepository;
    private final DiningTableService diningTableService;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public ReservationService(ReservationRepository reservationRepository,
                              DiningTableRepository diningTableRepository,
                              DiningTableService diningTableService,
                              UserRepository userRepository,
                              ActivityLogService activityLogService) {
        this.reservationRepository = reservationRepository;
        this.diningTableRepository = diningTableRepository;
        this.diningTableService = diningTableService;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    public Reservation createReservation(Reservation reservation) {
        System.out.println("=== DEBUG SERVICE: createReservation called with: " + reservation);
        System.out.println("=== DEBUG SERVICE: reservationDate value: " + (reservation != null ? reservation.getReservationDate() : "NULL"));
        System.out.println("=== DEBUG SERVICE: reservationTime value: " + (reservation != null ? reservation.getReservationTime() : "NULL"));
        
        if (reservation == null) {
            throw new ReservationValidationException("Rezervasyon verisi boş olamaz");
        }
        if (reservation.getTable() == null || reservation.getTable().getId() == null) {
            throw new ReservationValidationException("Masa bilgisi zorunludur. tableId alanı bulunamadı veya null");
        }
        if (reservation.getCreatedBy() == null || reservation.getCreatedBy().getId() == null) {
            throw new ReservationValidationException("Oluşturan kullanıcı zorunludur");
        }
        if (reservation.getReservationDate() == null) {
            throw new ReservationValidationException("Rezervasyon tarihi zorunludur");
        }
        if (reservation.getReservationTime() == null) {
            throw new ReservationValidationException("Rezervasyon saati zorunludur");
        }
        
        // GÜÇLÜ MASA DOĞRULAMA - Masa gerçekten var mı kontrol et
        System.out.println("=== DEBUG SERVICE: Validating table existence for ID: " + reservation.getTable().getId());
        DiningTable table = diningTableRepository.findById(reservation.getTable().getId())
                .orElseThrow(() -> {
                    System.err.println("=== ERROR: Table with ID " + reservation.getTable().getId() + " not found in database!");
                    return new TableNotFoundException("Masa bulunamadı! ID: " + reservation.getTable().getId() + 
                        ". Lütfen geçerli bir masa seçin.");
                });
        
        System.out.println("=== DEBUG SERVICE: Table found: " + table.getTableNumber() + " (ID: " + table.getId() + ")");
        
        if (table.getSalon() == null) {
            throw new ReservationValidationException("Masa bir salona bağlı olmalıdır");
        }
        
        System.out.println("=== DEBUG SERVICE: About to validate date: " + reservation.getReservationDate() + " and time: " + reservation.getReservationTime());
        
        // Tarih ve saat validasyonu
        if (!reservation.isReservationDateValid()) {
            throw new ReservationValidationException("Rezervasyon tarihi geçmiş bir tarih olamaz");
        }
        
        if (!reservation.isReservationTimeValid()) {
            throw new ReservationValidationException("Rezervasyon saati 08:00-23:00 arasında olmalıdır");
        }
        
        // ✅ Normalized string'i reservation'a set et
        reservation.setReservationTime(reservation.getReservationTime());
        System.out.println("=== DEBUG SERVICE: Reservation time set to normalized string: " + reservation.getReservationTime());
        
        if (reservation.getStatusId() != null && !ReservationStatusConstants.isValidStatusId(reservation.getStatusId())) {
            throw new ReservationValidationException("Geçersiz rezervasyon durumu");
        }

        // User doğrulama
        User user = userRepository.findById(reservation.getCreatedBy().getId())
                .orElseThrow(() -> ResourceNotFoundException.forId("User", reservation.getCreatedBy().getId()));
        reservation.setCreatedBy(user);

        // Çakışma kontrolü (CONFIRMED ve PENDING durumları için)
        ensureNoConflicts(table, reservation.getReservationDate(), reservation.getReservationTime(), null);

        // Rezervasyonu kaydet
        Reservation saved = reservationRepository.save(reservation);

        // Masa durumunu güncelle (rezervasyon oluşturulduğunda masa rezerve olur)
        updateTableStatusBasedOnReservation(saved);

        // Activity log (hata durumunda akışı bozmasın)
        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Reservation created for table ID " + table.getId() + " by user " + user.getName(),
                    "customerName", saved.getCustomerName(),
                    "tableId", table.getId().toString(),
                    "salonId", table.getSalon().getId().toString(),
                    "salonName", table.getSalon().getName(),
                    "reservationTime", saved.getReservationTime().toString(),
                    "statusId", saved.getStatusId().toString()
            );
            activityLogService.logActivity(user.getId(), "CREATE", "RESERVATION", saved.getId(), details);
        } catch (Exception ignored) { }

        return saved;
    }

    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        reservation.setStatusId(ReservationStatusConstants.CANCELLED);
        Reservation updated = reservationRepository.save(reservation);

        // Masa durumunu güncelle (iptal edildiğinde masa müsait olur)
        updateTableStatusBasedOnReservation(updated);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Reservation cancelled for customer " + reservation.getCustomerName(),
                    "customerName", reservation.getCustomerName(),
                    "tableId", reservation.getTable().getId().toString(),
                    "salonId", reservation.getTable().getSalon().getId().toString(),
                    "salonName", reservation.getTable().getSalon().getName()
            );
            activityLogService.logActivity(reservation.getCreatedBy().getId(), "CANCEL", "RESERVATION", reservationId, details);
        } catch (Exception ignored) { }

        return updated;
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        Long tableId = (reservation.getTable() != null) ? reservation.getTable().getId() : null;

        // Rezervasyonu sil
        reservationRepository.delete(reservation);

        // Silme sonrası masada başka aktif rezervasyon var mı kontrol et
        if (tableId != null) {
            List<Reservation> activeReservations = reservationRepository.findByTableIdAndStatusId(tableId, ReservationStatusConstants.CONFIRMED);
            List<Reservation> pendingReservations = reservationRepository.findByTableIdAndStatusId(tableId, ReservationStatusConstants.PENDING);
            
            boolean hasActiveReservation = (activeReservations != null && !activeReservations.isEmpty()) || 
                                         (pendingReservations != null && !pendingReservations.isEmpty());
            
            if (!hasActiveReservation) {
                // Başka aktif rezervasyon yoksa masayı AVAILABLE yap
                diningTableService.updateTableStatus(tableId, "AVAILABLE");
                System.out.println("=== DEBUG: Rezervasyon silindi, masa AVAILABLE yapıldı. Table ID: " + tableId);
            } else {
                System.out.println("=== DEBUG: Rezervasyon silindi ama başka aktif rezervasyon var. Table ID: " + tableId);
            }
        }

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Reservation deleted",
                    "customerName", reservation.getCustomerName(),
                    "tableId", reservation.getTable() != null ? reservation.getTable().getId().toString() : "Unknown",
                    "salonId", reservation.getTable() != null && reservation.getTable().getSalon() != null ?
                            reservation.getTable().getSalon().getId().toString() : "Unknown",
                    "salonName", reservation.getTable() != null && reservation.getTable().getSalon() != null ?
                            reservation.getTable().getSalon().getName() : "Unknown"
            );
            Long actorUserId = reservation.getCreatedBy() != null ? reservation.getCreatedBy().getId() : null;
            activityLogService.logActivity(actorUserId, "DELETE", "RESERVATION", reservationId, details);
        } catch (Exception ignored) { }
    }

    public Reservation updateReservation(Long reservationId, Reservation updatedReservation) {
        Reservation existing = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (updatedReservation.getStatusId() != null &&
                !ReservationStatusConstants.isValidStatusId(updatedReservation.getStatusId())) {
            throw new ReservationValidationException("Geçersiz rezervasyon durumu");
        }

        // Masa güncellenecekse doğrula ve ata
        if (updatedReservation.getTable() != null &&
                updatedReservation.getTable().getId() != null &&
                !updatedReservation.getTable().getId().equals(existing.getTable().getId())) {
            DiningTable newTable = diningTableRepository.findById(updatedReservation.getTable().getId())
                    .orElseThrow(() -> new TableNotFoundException("Table not found"));
            if (newTable.getSalon() == null) {
                throw new ReservationValidationException("Masa bir salona bağlı olmalıdır");
            }
            existing.setTable(newTable);
        }

        // Kullanıcı güncellenecekse doğrula ve ata
        if (updatedReservation.getCreatedBy() != null && updatedReservation.getCreatedBy().getId() != null) {
            User user = userRepository.findById(updatedReservation.getCreatedBy().getId())
                    .orElseThrow(() -> ResourceNotFoundException.forId("User", updatedReservation.getCreatedBy().getId()));
            existing.setCreatedBy(user);
        }

        if (updatedReservation.getCustomerName() != null) existing.setCustomerName(updatedReservation.getCustomerName());
        if (updatedReservation.getCustomerPhone() != null) existing.setCustomerPhone(updatedReservation.getCustomerPhone());
        if (updatedReservation.getSpecialRequests() != null) existing.setSpecialRequests(updatedReservation.getSpecialRequests());
        if (updatedReservation.getStatusId() != null) existing.setStatusId(updatedReservation.getStatusId());
        
        // Status ID değiştiyse masa durumunu güncelle
        if (updatedReservation.getStatusId() != null && !updatedReservation.getStatusId().equals(existing.getStatusId())) {
            updateTableStatusBasedOnReservation(existing);
        }

        // Zaman değişecekse doğrula ve çakışma kontrolü yap
        if (updatedReservation.getReservationDate() != null || updatedReservation.getReservationTime() != null) {
            try {
                LocalDate newDate = updatedReservation.getReservationDate() != null ? 
                    updatedReservation.getReservationDate() : existing.getReservationDate();
                LocalTime newTime = updatedReservation.getReservationTime() != null ? 
                    updatedReservation.getReservationTime() : existing.getReservationTime();
                
                if (newDate == null || newTime == null) {
                    throw new ReservationValidationException("Rezervasyon tarihi ve saati boş olamaz");
                }
                
                LocalDateTime newDateTime = LocalDateTime.of(newDate, newTime);
                
                if (newDateTime.isBefore(LocalDateTime.now())) {
                    throw new ReservationValidationException("Rezervasyon geçmiş bir zamana olamaz");
                }
                
                ensureNoConflicts(existing.getTable(), newDate, newTime, existing.getId());
                
                // Yeni değerleri set et
                if (updatedReservation.getReservationDate() != null) {
                    existing.setReservationDate(updatedReservation.getReservationDate());
                }
                if (updatedReservation.getReservationTime() != null) {
                    existing.setReservationTime(updatedReservation.getReservationTime());
                }
                
            } catch (ReservationValidationException e) {
                throw e; // Kendi exception'ımızı tekrar fırlat
            } catch (Exception e) {
                throw new ReservationValidationException("Rezervasyon zamanı güncellenirken hata: " + e.getMessage());
            }
        }

        Reservation saved = reservationRepository.save(existing);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Reservation updated for table ID " + saved.getTable().getId() +
                            " customer: " + saved.getCustomerName(),
                    "customerName", saved.getCustomerName(),
                    "tableId", saved.getTable().getId().toString(),
                    "salonId", saved.getTable().getSalon().getId().toString(),
                    "salonName", saved.getTable().getSalon().getName(),
                    "reservationTime", saved.getReservationDateTimeString(),
                    "statusId", saved.getStatusId().toString()
            );
            activityLogService.logActivity(saved.getCreatedBy().getId(), "UPDATE", "RESERVATION", reservationId, details);
        } catch (Exception ignored) { }

        return saved;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
    }

    public List<Reservation> getTableReservations(Long tableId) {
        return reservationRepository.findByTableId(tableId);
    }

    public List<Reservation> getSalonReservations(Long salonId) {
        return reservationRepository.findByTableSalonId(salonId);
    }

    public Reservation completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        reservation.setStatusId(ReservationStatusConstants.COMPLETED);
        Reservation updated = reservationRepository.save(reservation);

        // Masa durumunu güncelle (tamamlandığında masa müsait olur)
        updateTableStatusBasedOnReservation(updated);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Reservation completed for customer " + reservation.getCustomerName(),
                    "customerName", reservation.getCustomerName(),
                    "tableId", reservation.getTable().getId().toString(),
                    "salonId", reservation.getTable().getSalon().getId().toString(),
                    "salonName", reservation.getTable().getSalon().getName()
            );
            activityLogService.logActivity(reservation.getCreatedBy().getId(), "COMPLETE", "RESERVATION", reservationId, details);
        } catch (Exception ignored) { }

        return updated;
    }

    public Reservation markAsNoShow(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        reservation.setStatusId(ReservationStatusConstants.NO_SHOW);
        Reservation updated = reservationRepository.save(reservation);

        // Masa durumunu güncelle (gelmedi olarak işaretlendiğinde masa müsait olur)
        updateTableStatusBasedOnReservation(updated);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Reservation marked as no-show for customer " + reservation.getCustomerName(),
                    "customerName", reservation.getCustomerName(),
                    "tableId", reservation.getTable().getId().toString(),
                    "salonId", reservation.getTable().getSalon().getId().toString(),
                    "salonId", reservation.getTable().getSalon().getId().toString(),
                    "salonName", reservation.getTable().getSalon().getName()
            );
            activityLogService.logActivity(reservation.getCreatedBy().getId(), "NO_SHOW", "RESERVATION", reservationId, details);
        } catch (Exception ignored) { }

        return updated;
    }

    public List<Reservation> getReservationsByDateRange(LocalDate start, LocalDate end) {
        return reservationRepository.findByReservationTimeBetween(start, LocalTime.of(0, 0), end, LocalTime.of(23, 59, 59));
    }

    public List<Reservation> getTodayReservations() {
        LocalDate today = LocalDate.now();
        return reservationRepository.findByReservationTimeBetween(today, LocalTime.of(0, 0), today, LocalTime.of(23, 59, 59));
    }

    public List<Reservation> getReservationsByStatus(Integer statusId) {
        return reservationRepository.findByStatusId(statusId);
    }

    public List<Reservation> getActiveReservations() {
        return reservationRepository.findByStatusId(ReservationStatusConstants.CONFIRMED);
    }

    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByStatusId(ReservationStatusConstants.PENDING);
    }
    
    /**
     * Belirtilen masa için aktif rezervasyonları tamamlar (sipariş alındığında kullanılır)
     */
    @Transactional
    public List<Reservation> completeActiveReservationsForTable(Long tableId) {
        List<Reservation> activeReservations = reservationRepository.findByTableId(tableId).stream()
                .filter(reservation -> reservation.getStatusId() == ReservationStatusConstants.CONFIRMED || 
                                     reservation.getStatusId() == ReservationStatusConstants.PENDING)
                .collect(Collectors.toList());
        
        List<Reservation> completedReservations = new ArrayList<>();
        
        for (Reservation reservation : activeReservations) {
            reservation.setStatusId(ReservationStatusConstants.COMPLETED);
            Reservation updated = reservationRepository.save(reservation);
            completedReservations.add(updated);
            
            try {
                ObjectNode details = activityLogService.createDetailsNode(
                        "Reservation auto-completed due to order placement for customer " + reservation.getCustomerName(),
                        "customerName", reservation.getCustomerName(),
                        "tableId", tableId.toString(),
                        "reason", "ORDER_PLACED"
                );
                activityLogService.logActivity(reservation.getCreatedBy().getId(), "AUTO_COMPLETE", "RESERVATION", reservation.getId(), details);
            } catch (Exception ignored) { }
        }
        
        return completedReservations;
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Rezervasyon durumuna göre masa durumunu günceller
     */
    private void updateTableStatusBasedOnReservation(Reservation reservation) {
        try {
            if (reservation.getTable() != null && reservation.getTable().getId() != null) {
                Long tableId = reservation.getTable().getId();
                Integer statusId = reservation.getStatusId();
                
                if (statusId != null) {
                    switch (statusId) {
                        case ReservationStatusConstants.CONFIRMED:
                        case ReservationStatusConstants.PENDING:
                            // Masa rezerve edildi
                            diningTableService.updateTableStatus(tableId, "RESERVED");
                            System.out.println("=== DEBUG: Masa durumu RESERVED olarak güncellendi. Table ID: " + tableId);
                            break;
                        case ReservationStatusConstants.CANCELLED:
                        case ReservationStatusConstants.COMPLETED:
                        case ReservationStatusConstants.NO_SHOW:
                            // Masa müsait hale geldi
                            diningTableService.updateTableStatus(tableId, "AVAILABLE");
                            System.out.println("=== DEBUG: Masa durumu AVAILABLE olarak güncellendi. Table ID: " + tableId);
                            break;
                        default:
                            System.out.println("=== DEBUG: Bilinmeyen rezervasyon durumu: " + statusId);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("=== ERROR: Masa durumu güncellenirken hata: " + e.getMessage());
            // Ana işlemi bozma, sadece log
        }
    }

    private void ensureNoConflicts(DiningTable table, LocalDate reservationDate, LocalTime reservationTime, Long excludeReservationId) {
        try {
            if (reservationDate == null) {
                throw new ReservationValidationException("Rezervasyon tarihi boş olamaz");
            }
            if (reservationTime == null) {
                throw new ReservationValidationException("Rezervasyon saati boş olamaz");
            }
            
            // LocalDateTime oluştur (conflict check için)
            LocalDateTime time = LocalDateTime.of(reservationDate, reservationTime);
            System.out.println("=== DEBUG: Conflict check - created time: " + time + " from date: " + reservationDate + " and time: " + reservationTime);
            
            LocalDateTime start = time.minusMinutes(CONFLICT_WINDOW_MINUTES);
            LocalDateTime end = time.plusMinutes(CONFLICT_WINDOW_MINUTES);

            List<Reservation> conflicts = new ArrayList<>();
            conflicts.addAll(reservationRepository.findByTableAndStatusIdAndDateTimeBetween(table, ReservationStatusConstants.CONFIRMED, start.toLocalDate(), start.toLocalTime(), end.toLocalDate(), end.toLocalTime()));
            conflicts.addAll(reservationRepository.findByTableAndStatusIdAndDateTimeBetween(table, ReservationStatusConstants.PENDING, start.toLocalDate(), start.toLocalTime(), end.toLocalDate(), end.toLocalTime()));

            if (excludeReservationId != null) {
                conflicts.removeIf(r -> excludeReservationId.equals(r.getId()));
            }

            if (!conflicts.isEmpty()) {
                throw new ReservationConflictException("Bu zaman aralığında aynı masa için çakışan rezervasyon bulunmaktadır");
            }
        } catch (ReservationValidationException e) {
            throw e; // Kendi exception'ımızı tekrar fırlat
        } catch (Exception e) {
            throw new ReservationValidationException(
                "Rezervasyon zamanı doğrulanamadı: tarih=" + reservationDate + ", saat=" + reservationTime + "\n" +
                "Hata detayı: " + e.getMessage()
            );
        }
    }

    /**
     * Replaces non-breaking spaces and unicode dashes with standard chars, and
     * collapses multiple whitespaces to single space between date and time.
     * Enhanced to handle frontend date formats more robustly.
     */
    private String normalizeReservationTimeString(String input) {
        if (input == null) return null;
        
        System.out.println("=== DEBUG: Normalizing input: '" + input + "'");
        
        String s = input
                // Unicode spaces to standard space
                .replace('\u00A0', ' ')      // Non-breaking space
                .replace('\u2007', ' ')      // Figure space
                .replace('\u202F', ' ')      // Narrow no-break space
                .replace('\u2000', ' ')      // En quad
                .replace('\u2001', ' ')      // Em quad
                .replace('\u2002', ' ')      // En space
                .replace('\u2003', ' ')      // Em space
                .replace('\u2004', ' ')      // Three-per-em space
                .replace('\u2005', ' ')      // Four-per-em space
                .replace('\u2006', ' ')      // Six-per-em space
                // Unicode dashes to standard dash
                .replace('\u2013', '-')      // En dash
                .replace('\u2014', '-')      // Em dash
                .replace('\u2015', '-')      // Horizontal bar
                .replace('\u2212', '-')      // Minus sign
                .trim();
        
        // Collapse any whitespace runs to single space
        s = s.replaceAll("\\s+", " ");
        
        // Normalize date separators to standard dash
        // e.g., 2025.12.21 12:00 or 2025/12/21 12:00 → 2025-12-21 12:00
        s = s.replace('/', '-').replace('.', '-');
        
        // Ensure proper format: YYYY-MM-DD HH:MM
        // If we have something like "2025-12-21 12:14", ensure it's properly formatted
        if (s.matches("\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{2}")) {
            // Pad single digits with leading zeros
            String[] parts = s.split("\\s+");
            if (parts.length == 2) {
                String datePart = parts[0];
                String timePart = parts[1];
                
                // Pad date parts
                String[] dateParts = datePart.split("-");
                if (dateParts.length == 3) {
                    String year = dateParts[0];
                    String month = String.format("%02d", Integer.parseInt(dateParts[1]));
                    String day = String.format("%02d", Integer.parseInt(dateParts[2]));
                    datePart = year + "-" + month + "-" + day;
                }
                
                // Pad time parts
                String[] timeParts = timePart.split(":");
                if (timeParts.length == 2) {
                    String hour = String.format("%02d", Integer.parseInt(timeParts[0]));
                    String minute = String.format("%02d", Integer.parseInt(timeParts[1]));
                    timePart = hour + ":" + minute;
                }
                
                s = datePart + " " + timePart;
            }
        }
        
        System.out.println("=== DEBUG: Normalized result: '" + s + "'");
        return s;
    }
}
