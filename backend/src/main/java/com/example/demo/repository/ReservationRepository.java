package com.example.demo.repository;

import com.example.demo.model.DiningTable;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTableId(Long tableId);

    /**
     * Belirli bir masa, durum ve zaman aralığına göre çakışan rezervasyonları getirir.
     * ReservationService.validateReservation() içinde çakışma kontrolü için kullanılır.
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.table = :table " +
            "AND r.statusId = :statusId " +
            "AND (r.reservationDate > :startDate OR (r.reservationDate = :startDate AND r.reservationTime >= :startTime)) " +
            "AND (r.reservationDate < :endDate OR (r.reservationDate = :endDate AND r.reservationTime < :endTime))")
    List<Reservation> findByTableAndStatusIdAndDateTimeBetween(
            @Param("table") DiningTable table,
            @Param("statusId") Integer statusId,
            @Param("startDate") LocalDate startDate,
            @Param("startTime") LocalTime startTime,
            @Param("endDate") LocalDate endDate,
            @Param("endTime") LocalTime endTime
    );

    List<Reservation> findByStatusId(Integer statusId);

    // Find reservations by date
    @Query("SELECT r FROM Reservation r WHERE r.reservationDate = :reservationDate")
    List<Reservation> findByReservationDate(@Param("reservationDate") LocalDate reservationDate);

    // Find reservations between date range
    @Query("SELECT r FROM Reservation r WHERE (r.reservationDate > :startDate OR (r.reservationDate = :startDate AND r.reservationTime >= :startTime)) " +
           "AND (r.reservationDate < :endDate OR (r.reservationDate = :endDate AND r.reservationTime < :endTime))")
    List<Reservation> findByReservationTimeBetween(
            @Param("startDate") LocalDate startDate,
            @Param("startTime") LocalTime startTime,
            @Param("endDate") LocalDate endDate,
            @Param("endTime") LocalTime endTime
    );

    List<Reservation> findByCreatedBy(User createdBy);

    List<Reservation> findByCustomerNameContainingIgnoreCase(String customerName);

    List<Reservation> findByCustomerPhone(String customerPhone);

    // Order by date and time
    List<Reservation> findAllByOrderByReservationDateAscReservationTimeAsc();

    List<Reservation> findByStatusIdOrderByReservationDateAscReservationTimeAsc(Integer statusId);

    @Query("SELECT r FROM Reservation r WHERE (r.reservationDate > :startDate OR (r.reservationDate = :startDate AND r.reservationTime >= :startTime)) " +
           "AND (r.reservationDate < :endDate OR (r.reservationDate = :endDate AND r.reservationTime < :endTime))")
    List<Reservation> findReservationsInTimeRange(
            @Param("startDate") LocalDate startDate,
            @Param("startTime") LocalTime startTime,
            @Param("endDate") LocalDate endDate,
            @Param("endTime") LocalTime endTime
    );

    List<Reservation> findByCustomerNameContainingIgnoreCaseAndStatusId(String customerName, Integer statusId);

    List<Reservation> findByTableIdAndStatusId(Long tableId, Integer statusId);

    /**
     * Belirli bir salona ait tüm rezervasyonları getirir.
     */
    @Query("SELECT r FROM Reservation r WHERE r.table.salon.id = :salonId")
    List<Reservation> findByTableSalonId(@Param("salonId") Long salonId);
}