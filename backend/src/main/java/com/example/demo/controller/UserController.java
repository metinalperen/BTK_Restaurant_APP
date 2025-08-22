package com.example.demo.controller;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.validation.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.model.Role;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserRoleId;
import com.example.demo.service.RoleMappingService;
import com.example.demo.dto.request.PhoneNumberUpdateRequestDTO;
import com.example.demo.exception.user.UserNotFoundException;
import com.example.demo.exception.user.UserPhotoNotFoundException;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(
        name = "User Management",
        description = "API's for managing users in the system (CRUD operations, retrieval by ID, and listing all users)."
)
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final UserRepository userRepository;
    private final RoleMappingService roleMappingService;


    public UserController(UserService userService, ModelMapper modelMapper, UserValidator userValidator,
                          UserRepository userRepository, RoleMappingService roleMappingService) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
        this.userRepository = userRepository;
        this.roleMappingService = roleMappingService;
    }
    @Validated
    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user with the provided details."
    )
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        List<String> validationErrors = userValidator.validateUser(userRequestDTO);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Find or create the role
        Role role = roleMappingService.getRoleByName(userRequestDTO.getRoleName());

        // Create user
        User user = modelMapper.map(userRequestDTO, User.class);
        user.setPassword(userRequestDTO.getPassword());
        User createdUser = userService.createUser(user);

        // Create and save UserRole relationship
        UserRole userRole = new UserRole();
        UserRoleId userRoleId = new UserRoleId(createdUser.getId(), role.getId());
        userRole.setId(userRoleId);
        userRole.setUser(createdUser);
        userRole.setRole(role);

        // Add to user's roles set
        createdUser.getUserRoles().add(userRole);

        // Save the user with the role relationship
        User savedUser = userRepository.save(createdUser);

        return ResponseEntity.ok(convertUserToDTO(savedUser));
    }

    @Validated
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users."
    )
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active users", description = "Retrieves users with isActive=true")
    public List<UserResponseDTO> getActiveUsers() {
        return userService.getActiveUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get inactive users", description = "Retrieves users with isActive=false")
    public List<UserResponseDTO> getInactiveUsers() {
        return userService.getInactiveUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by their ID."
    )
    public UserResponseDTO getUserById(
            @Parameter(description = "ID of the user to be retrieved", required = true)
            @PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(UserNotFoundException::new);
        return convertUserToDTO(user);
    }

    @Validated
    @PutMapping("/{id}")
    @Operation(
            summary = "Update a user",
            description = "Updates an existing user's details."
    )
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID of the user to be updated", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        List<String> validationErrors = userValidator.validateUser(userRequestDTO);
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        User updatedUser = modelMapper.map(userRequestDTO, User.class);
        updatedUser.setPasswordHash(userRequestDTO.getPassword());
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(convertUserToDTO(user));
    }

    @Validated
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a user",
            description = "Deletes a user by their ID."
    )
    public void deleteUser(
            @Parameter(description = "ID of the user to be deleted", required = true)
            @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Validated
    @PatchMapping("/{id}/active")
    @Operation(summary = "Set user active status", description = "Sets isActive to true or false for a user")
    public ResponseEntity<UserResponseDTO> setUserActive(
            @PathVariable Long id,
            @RequestParam("active") boolean active) {
        User updated = userService.setUserActive(id, active);
        return ResponseEntity.ok(convertUserToDTO(updated));
    }

    @Validated
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload user photo",
            description = "Uploads a profile photo and updates user's photoUrl."
    )
    public ResponseEntity<Void> uploadUserPhoto(
            @Parameter(description = "ID of the user to upload a photo for", required = true)
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        try {
            userService.uploadUserPhoto(id, file);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Validated
    @GetMapping(value = "/{id}/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(
            summary = "Get user photo",
            description = "Returns the user's profile photo as image."
    )
    public ResponseEntity<?> getUserPhoto(
            @Parameter(description = "ID of the user to get a photo for", required = true)
            @PathVariable Long id) {
        try {
            byte[] photoBytes = userService.loadUserPhoto(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(photoBytes);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Fotoğraf yüklenemedi");
        }
    }

    @Validated
    @DeleteMapping("/{id}/photo")
    @Operation(
            summary = "Delete user photo",
            description = "Deletes the user's profile photo from storage and DB."
    )
    public ResponseEntity<Void> deleteUserPhoto(
            @Parameter(description = "ID of the user to delete a photo for", required = true)
            @PathVariable Long id) {
        try {
            userService.deleteUserPhoto(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException | UserPhotoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }


    }



    private UserResponseDTO convertUserToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());

        if (user.getPhotoBlob() != null) {
            dto.setPhotoBase64(Base64.getEncoder().encodeToString(user.getPhotoBlob()));
        }
        else {
            try {
                dto.setPhotoBase64(Base64.getEncoder().encodeToString(Objects.requireNonNull(getClass().getResourceAsStream("/img/default.jpg")).readAllBytes()));
            }
            catch (IOException e) {
                throw new RuntimeException("Default photo not found", e);
            }
        }

        dto.setCreatedAt(user.getCreatedAt());

        Set<Integer> roleCodes = user.getUserRoles().stream()
                .map(userRole -> roleMappingService.getRoleIdByName(userRole.getRole().getName()).intValue())
                .collect(Collectors.toSet());
        dto.setRoles(roleCodes);
        return dto;
    }
    // NEW ENDPOINT: Update phone number only
    @PatchMapping("/{id}/phone")
    @Operation(
            summary = "Update user phone number",
            description = "Updates only the phone number of a specific user."
    )

    public ResponseEntity<UserResponseDTO> updateUserPhoneNumber(
            @Parameter(description = "ID of the user to update phone number for", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PhoneNumberUpdateRequestDTO phoneNumberRequest) {

        try {
            User updatedUser = userService.updatePhoneNumber(id, phoneNumberRequest.getPhoneNumber());
            return ResponseEntity.ok(convertUserToDTO(updatedUser));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
