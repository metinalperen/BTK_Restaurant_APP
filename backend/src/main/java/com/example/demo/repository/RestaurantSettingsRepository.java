package com.example.demo.repository;

import com.example.demo.model.RestaurantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantSettingsRepository extends JpaRepository<RestaurantSettings, Long> {
	@Query("SELECT r FROM RestaurantSettings r ORDER BY r.updatedAt DESC")
	java.util.List<RestaurantSettings> findAllOrderByUpdatedAtDesc();

	/**
	 * Find the single restaurant settings record.
	 * Since there should only be one row, this method returns the first available record.
	 */
	@Query("SELECT r FROM RestaurantSettings r ORDER BY r.updatedAt DESC LIMIT 1")
	Optional<RestaurantSettings> findSingleSettings();

	/**
	 * @deprecated Use findSingleSettings() instead for better performance
	 */
	@Deprecated
	default Optional<RestaurantSettings> findLatest() {
		java.util.List<RestaurantSettings> list = findAllOrderByUpdatedAtDesc();
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}
} 