package com.example.demo.dto.request;

import java.time.LocalTime;

public class RestaurantSettingsRequestDTO {
	private String restaurantName;

	private LocalTime openTime;

	private LocalTime closeTime;

	private Integer lastReservationCutoffMinutes;

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