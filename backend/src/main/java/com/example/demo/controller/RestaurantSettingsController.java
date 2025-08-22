package com.example.demo.controller;

import com.example.demo.dto.request.RestaurantSettingsRequestDTO;
import com.example.demo.dto.response.RestaurantSettingsResponseDTO;
import com.example.demo.service.RestaurantSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Restaurant Settings", description = "APIs for restaurant name and working hours")
@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class RestaurantSettingsController {

	private final RestaurantSettingsService service;

	public RestaurantSettingsController(RestaurantSettingsService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get current settings")
	public ResponseEntity<RestaurantSettingsResponseDTO> getSettings() {
		return ResponseEntity.ok(service.getSettings());
	}

	@GetMapping("/refresh")
	@Operation(summary = "Get current settings and refresh the updated_at timestamp")
	public ResponseEntity<RestaurantSettingsResponseDTO> getSettingsWithRefresh() {
		return ResponseEntity.ok(service.getSettings(true));
	}

	@PutMapping
	@Operation(summary = "Create or update settings")
	public ResponseEntity<RestaurantSettingsResponseDTO> upsertSettings(@Valid @RequestBody RestaurantSettingsRequestDTO request) {
		return ResponseEntity.ok(service.upsertSettings(request));
	}

	@PatchMapping
	@Operation(summary = "Update specific settings fields (partial update)")
	public ResponseEntity<RestaurantSettingsResponseDTO> updateSettings(@RequestBody RestaurantSettingsRequestDTO request) {
		return ResponseEntity.ok(service.updateSettings(request));
	}

	@PostMapping("/enforce-constraint")
	@Operation(summary = "Enforce single-row constraint and clean up duplicates")
	public ResponseEntity<String> enforceSingleRowConstraint() {
		service.enforceSingleRowConstraint();
		return ResponseEntity.ok("Single-row constraint enforced successfully");
	}

	@GetMapping("/count")
	@Operation(summary = "Get the current count of settings records (should always be 1)")
	public ResponseEntity<Long> getSettingsCount() {
		return ResponseEntity.ok(service.getSettingsCount());
	}

	@PostMapping("/initialize")
	@Operation(summary = "Initialize restaurant settings if none exist")
	public ResponseEntity<String> initializeSettings() {
		service.initializeSettingsIfNeeded();
		return ResponseEntity.ok("Restaurant settings initialized successfully");
	}

	@PostMapping("/refresh-timestamp")
	@Operation(summary = "Manually refresh the updated_at timestamp")
	public ResponseEntity<RestaurantSettingsResponseDTO> refreshTimestamp() {
		return ResponseEntity.ok(service.refreshTimestamp());
	}
} 