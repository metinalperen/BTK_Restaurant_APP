package com.example.demo.service;

import com.example.demo.dto.request.RestaurantSettingsRequestDTO;
import com.example.demo.dto.response.RestaurantSettingsResponseDTO;
import com.example.demo.model.RestaurantSettings;
import com.example.demo.repository.RestaurantSettingsRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RestaurantSettingsService {
	private final RestaurantSettingsRepository repository;
	private final ModelMapper modelMapper;
	private final ActivityLogService activityLogService;

	public RestaurantSettingsService(RestaurantSettingsRepository repository, 
	                               ModelMapper modelMapper,
	                               ActivityLogService activityLogService) {
		this.repository = repository;
		this.modelMapper = modelMapper;
		this.activityLogService = activityLogService;
	}

	public RestaurantSettingsResponseDTO getSettings() {
		RestaurantSettings settings = repository.findSingleSettings().orElseGet(() -> createDefault());
		return toResponse(settings);
	}

	/**
	 * Get settings with option to refresh the updated_at timestamp
	 */
	public RestaurantSettingsResponseDTO getSettings(boolean refreshTimestamp) {
		if (refreshTimestamp) {
			RestaurantSettings settings = getSettingsEntityAndRefreshTimestamp();
			return toResponse(settings);
		} else {
			return getSettings();
		}
	}

	/**
	 * Get the RestaurantSettings entity directly (for internal use by other services)
	 */
	public RestaurantSettings getSettingsEntity() {
		return repository.findSingleSettings().orElseGet(() -> createDefault());
	}

	/**
	 * Get the RestaurantSettings entity and refresh the updated_at timestamp
	 * Useful when you want to track when settings were last accessed
	 */
	public RestaurantSettings getSettingsEntityAndRefreshTimestamp() {
		RestaurantSettings settings = repository.findSingleSettings().orElseGet(() -> createDefault());
		
		// Update the timestamp to reflect this access
		settings.setUpdatedAt(LocalDateTime.now());
		return repository.save(settings);
	}

	/**
	 * Enforce single-row constraint by cleaning up any duplicate settings
	 * This method should be called periodically or when issues are detected
	 */
	@Transactional
	public void enforceSingleRowConstraint() {
		List<RestaurantSettings> allSettings = repository.findAll();
		
		if (allSettings.size() <= 1) {
			return; // No cleanup needed
		}
		
		// Keep only the most recent settings
		RestaurantSettings mostRecent = allSettings.stream()
			.sorted((s1, s2) -> s2.getUpdatedAt().compareTo(s1.getUpdatedAt()))
			.findFirst()
			.orElse(null);
		
		if (mostRecent != null) {
			// Delete all other settings
			allSettings.stream()
				.filter(s -> !s.getId().equals(mostRecent.getId()))
				.forEach(repository::delete);
			
			// Update the timestamp of the kept settings to reflect the cleanup operation
			mostRecent.setUpdatedAt(LocalDateTime.now());
			repository.save(mostRecent);
			
			// Log the cleanup
			try {
				ObjectNode details = activityLogService.createDetailsNode(
					"Duplicate restaurant settings cleaned up - kept most recent settings",
					"keptSettingsId", mostRecent.getId().toString(),
					"deletedCount", String.valueOf(allSettings.size() - 1)
				);
				activityLogService.logActivity("CLEANUP", "RESTAURANT_SETTINGS", mostRecent.getId(), details);
			} catch (Exception ignored) { }
		}
	}

	/**
	 * Get the current count of settings records (should always be 1)
	 */
	@Transactional(readOnly = true)
	public long getSettingsCount() {
		return repository.count();
	}

	/**
	 * Manually refresh the updated_at timestamp without changing any data
	 * Useful for keeping track of when settings were last accessed
	 */
	@Transactional
	public RestaurantSettingsResponseDTO refreshTimestamp() {
		RestaurantSettings settings = repository.findSingleSettings()
			.orElseThrow(() -> new IllegalStateException("No restaurant settings found. Please create settings first."));
		
		settings.setUpdatedAt(LocalDateTime.now());
		RestaurantSettings saved = repository.save(settings);
		
		// Log the timestamp refresh
		try {
			ObjectNode details = activityLogService.createDetailsNode(
				"Restaurant settings timestamp refreshed",
				"previousUpdatedAt", settings.getUpdatedAt().toString(),
				"newUpdatedAt", saved.getUpdatedAt().toString()
			);
			activityLogService.logActivity("TIMESTAMP_REFRESH", "RESTAURANT_SETTINGS", saved.getId(), details);
		} catch (Exception ignored) { }
		
		return toResponse(saved);
	}

	/**
	 * Initialize restaurant settings if none exist (useful for system startup)
	 */
	@Transactional
	public void initializeSettingsIfNeeded() {
		if (repository.count() == 0) {
			createDefault();
		} else {
			// If settings exist, ensure they have a recent updated_at timestamp
			RestaurantSettings existingSettings = repository.findSingleSettings().orElse(null);
			if (existingSettings != null) {
				// Update timestamp to reflect the initialization check
				existingSettings.setUpdatedAt(LocalDateTime.now());
				repository.save(existingSettings);
			}
		}
	}

	/**
	 * Update only specific fields of restaurant settings (partial update)
	 * This is a convenience method for partial updates
	 */
	public RestaurantSettingsResponseDTO updateSettings(RestaurantSettingsRequestDTO request) {
		// For partial updates, we need existing settings
		RestaurantSettings existingSettings = repository.findSingleSettings()
			.orElseThrow(() -> new IllegalStateException("No restaurant settings found. Please create settings first."));
		
		// Validate provided fields
		validateProvidedFields(request);
		
		// Detect changes and log them
		logSettingsChanges(existingSettings, request);
		
		// Only update fields that are provided (partial update)
		if (request.getRestaurantName() != null) {
			existingSettings.setRestaurantName(request.getRestaurantName());
		}
		if (request.getOpenTime() != null) {
			existingSettings.setOpenTime(request.getOpenTime());
		}
		if (request.getCloseTime() != null) {
			existingSettings.setCloseTime(request.getCloseTime());
		}
		if (request.getLastReservationCutoffMinutes() != null) {
			existingSettings.setLastReservationCutoffMinutes(request.getLastReservationCutoffMinutes());
		}
		
		existingSettings.setUpdatedAt(LocalDateTime.now());
		RestaurantSettings saved = repository.save(existingSettings);
		return toResponse(saved);
	}

	public RestaurantSettingsResponseDTO upsertSettings(RestaurantSettingsRequestDTO request) {
		// Validate provided fields
		validateProvidedFields(request);
		
		// Get existing settings or create new ones
		RestaurantSettings existingSettings = repository.findSingleSettings().orElse(null);
		RestaurantSettings settings;
		
		if (existingSettings == null) {
			// Create new settings - all fields are required for creation
			settings = new RestaurantSettings();
			settings.setCreatedAt(LocalDateTime.now());
			settings.setUpdatedAt(LocalDateTime.now());
			
			// Set all required fields for new settings
			settings.setRestaurantName(request.getRestaurantName() != null ? request.getRestaurantName() : "Restoran Yönetim Sistemi");
			settings.setOpenTime(request.getOpenTime() != null ? request.getOpenTime() : java.time.LocalTime.of(8, 0));
			settings.setCloseTime(request.getCloseTime() != null ? request.getCloseTime() : java.time.LocalTime.of(23, 0));
			settings.setLastReservationCutoffMinutes(request.getLastReservationCutoffMinutes() != null ? request.getLastReservationCutoffMinutes() : 180);
			
			// Log creation
			try {
				ObjectNode details = activityLogService.createDetailsNode(
					"Restaurant settings created with initial values",
					"restaurantName", settings.getRestaurantName(),
					"openTime", settings.getOpenTime().toString(),
					"closeTime", settings.getCloseTime().toString(),
					"lastReservationCutoffMinutes", settings.getLastReservationCutoffMinutes().toString()
				);
				activityLogService.logActivity("CREATE", "RESTAURANT_SETTINGS", null, details);
			} catch (Exception ignored) { }
		} else {
			// Update existing settings - only update provided fields
			settings = existingSettings;
			
			// Detect changes and log them
			logSettingsChanges(existingSettings, request);
			
			// Only update fields that are provided (partial update)
			if (request.getRestaurantName() != null) {
				settings.setRestaurantName(request.getRestaurantName());
			}
			if (request.getOpenTime() != null) {
				settings.setOpenTime(request.getOpenTime());
			}
			if (request.getCloseTime() != null) {
				settings.setCloseTime(request.getCloseTime());
			}
			if (request.getLastReservationCutoffMinutes() != null) {
				settings.setLastReservationCutoffMinutes(request.getLastReservationCutoffMinutes());
			}
		}
		
		settings.setUpdatedAt(LocalDateTime.now());
		RestaurantSettings saved = repository.save(settings);
		return toResponse(saved);
	}

	/**
	 * Validate provided fields in the request
	 */
	private void validateProvidedFields(RestaurantSettingsRequestDTO request) {
		// Validate restaurant name if provided
		if (request.getRestaurantName() != null && request.getRestaurantName().trim().isEmpty()) {
			throw new IllegalArgumentException("Restaurant name cannot be empty");
		}
		
		// Validate open time if provided
		if (request.getOpenTime() != null) {
			// Basic time validation
			if (request.getOpenTime().getHour() < 0 || request.getOpenTime().getHour() > 23) {
				throw new IllegalArgumentException("Invalid open time hour");
			}
		}
		
		// Validate close time if provided
		if (request.getCloseTime() != null) {
			// Basic time validation
			if (request.getCloseTime().getHour() < 0 || request.getCloseTime().getHour() > 23) {
				throw new IllegalArgumentException("Invalid close time hour");
			}
		}
		
		// Validate last reservation cutoff if provided
		if (request.getLastReservationCutoffMinutes() != null) {
			if (request.getLastReservationCutoffMinutes() < 0) {
				throw new IllegalArgumentException("Last reservation cutoff minutes cannot be negative");
			}
		}
		
		// Validate that open time is before close time if both are provided
		if (request.getOpenTime() != null && request.getCloseTime() != null) {
			if (!request.getOpenTime().isBefore(request.getCloseTime())) {
				throw new IllegalArgumentException("Open time must be before close time");
			}
		}
	}

	private void logSettingsChanges(RestaurantSettings existing, RestaurantSettingsRequestDTO request) {
		try {
			ObjectNode details = activityLogService.createObjectNode();
			details.put("message", "Restaurant settings updated");
			details.put("timestamp", LocalDateTime.now().toString());
			
			boolean hasChanges = false;
			
			// Check restaurant name changes (only if provided)
			if (request.getRestaurantName() != null && !existing.getRestaurantName().equals(request.getRestaurantName())) {
				details.put("oldRestaurantName", existing.getRestaurantName());
				details.put("newRestaurantName", request.getRestaurantName());
				hasChanges = true;
			}
			
			// Check open time changes (only if provided)
			if (request.getOpenTime() != null && !existing.getOpenTime().equals(request.getOpenTime())) {
				details.put("oldOpenTime", existing.getOpenTime().toString());
				details.put("newOpenTime", request.getOpenTime().toString());
				hasChanges = true;
			}
			
			// Check close time changes (only if provided)
			if (request.getCloseTime() != null && !existing.getCloseTime().equals(request.getCloseTime())) {
				details.put("oldCloseTime", existing.getCloseTime().toString());
				details.put("newCloseTime", request.getCloseTime().toString());
				hasChanges = true;
			}
			
			// Check last reservation cutoff changes (only if provided)
			if (request.getLastReservationCutoffMinutes() != null && !existing.getLastReservationCutoffMinutes().equals(request.getLastReservationCutoffMinutes())) {
				details.put("oldLastReservationCutoffMinutes", existing.getLastReservationCutoffMinutes().toString());
				details.put("newLastReservationCutoffMinutes", request.getLastReservationCutoffMinutes().toString());
				hasChanges = true;
			}
			
			// Only log if there are actual changes
			if (hasChanges) {
				activityLogService.logActivity("UPDATE", "RESTAURANT_SETTINGS", existing.getId(), details);
			}
		} catch (Exception ignored) { }
	}

	private RestaurantSettings createDefault() {
		// First, check if there are any existing settings and clean them up
		List<RestaurantSettings> allSettings = repository.findAll();
		if (!allSettings.isEmpty()) {
			// If there are multiple settings, keep only the most recent one
			RestaurantSettings mostRecent = allSettings.stream()
				.sorted((s1, s2) -> s2.getUpdatedAt().compareTo(s1.getUpdatedAt()))
				.findFirst()
				.orElse(null);
			
			if (mostRecent != null) {
				// Delete all other settings
				allSettings.stream()
					.filter(s -> !s.getId().equals(mostRecent.getId()))
					.forEach(repository::delete);
				
				return mostRecent;
			}
		}
		
		// Create new default settings
		RestaurantSettings s = new RestaurantSettings();
		s.setRestaurantName("Restoran Yönetim Sistemi");
		s.setOpenTime(java.time.LocalTime.of(8, 0));
		s.setCloseTime(java.time.LocalTime.of(23, 0));
		s.setLastReservationCutoffMinutes(180);
		s.setCreatedAt(LocalDateTime.now());
		s.setUpdatedAt(LocalDateTime.now());
		
		RestaurantSettings saved = repository.save(s);
		
		// Log the creation of default settings
		try {
			ObjectNode details = activityLogService.createDetailsNode(
				"Default restaurant settings created automatically",
				"restaurantName", saved.getRestaurantName(),
				"openTime", saved.getOpenTime().toString(),
				"closeTime", saved.getCloseTime().toString(),
				"lastReservationCutoffMinutes", saved.getLastReservationCutoffMinutes().toString()
			);
			activityLogService.logActivity("CREATE", "RESTAURANT_SETTINGS", saved.getId(), details);
		} catch (Exception ignored) { }
		
		// Ensure single-row constraint is maintained
		enforceSingleRowConstraint();
		
		return saved;
	}

	private RestaurantSettingsResponseDTO toResponse(RestaurantSettings entity) {
		RestaurantSettingsResponseDTO dto = new RestaurantSettingsResponseDTO();
		dto.setId(entity.getId());
		dto.setRestaurantName(entity.getRestaurantName());
		dto.setOpenTime(entity.getOpenTime());
		dto.setCloseTime(entity.getCloseTime());
		dto.setLastReservationCutoffMinutes(entity.getLastReservationCutoffMinutes());
		return dto;
	}
} 