package com.example.demo.dto.response;

import java.time.LocalTime;

public class RestaurantSettingsResponseDTO {
	private Long id;
	private String restaurantName;
	private LocalTime openTime;
	private LocalTime closeTime;
	private Integer lastReservationCutoffMinutes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}

	public LocalTime getOpenTime() {
		return openTime;
	}

	public void setOpenTime(LocalTime openTime) {
		this.openTime = openTime;
	}

	public LocalTime getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(LocalTime closeTime) {
		this.closeTime = closeTime;
	}

	public Integer getLastReservationCutoffMinutes() {
		return lastReservationCutoffMinutes;
	}

	public void setLastReservationCutoffMinutes(Integer lastReservationCutoffMinutes) {
		this.lastReservationCutoffMinutes = lastReservationCutoffMinutes;
	}
} 