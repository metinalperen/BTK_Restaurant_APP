package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.model.Role;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserRoleId;
import org.modelmapper.ModelMapper;
import com.example.demo.exception.user.UserNotFoundException;
import com.example.demo.exception.user.UserAlreadyExistsException;
import com.example.demo.exception.user.UserPhotoNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final RoleMappingService roleMappingService;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       ActivityLogService activityLogService,
                       RoleMappingService roleMappingService,
                       ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
        this.roleMappingService = roleMappingService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public User registerUser(UserRequestDTO userRequestDTO) {
        // Duplicate email check
        if (userRepository.findByEmail(userRequestDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Bu e-posta ile kayıtlı bir kullanıcı zaten mevcut.");
        }
        // Find the role by name
        Role role = roleMappingService.getRoleByName(userRequestDTO.getRoleName());

        // Create user
        User user = modelMapper.map(userRequestDTO, User.class);
        user.setPassword(userRequestDTO.getPassword());
        User createdUser = createUser(user);

        // Create and save UserRole relationship
        UserRole userRole = new UserRole();
        UserRoleId userRoleId = new UserRoleId(createdUser.getId(), role.getId());
        userRole.setId(userRoleId);
        userRole.setUser(createdUser);
        userRole.setRole(role);

        // Add to user's roles set
        createdUser.getUserRoles().add(userRole);

        // Save the user with the role relationship
        return userRepository.save(createdUser);
    }

    // CREATE
    @Transactional
    public User createUser(User user) {
        // Duplicate email check
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Bu e-posta ile kayıtlı bir kullanıcı zaten mevcut.");
        }
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        /* Set default for photoBlob
        try {
            //Look for default.jpg file in resources/img
            byte[] defaultPhoto = Objects.requireNonNull(getClass().getResourceAsStream("/img/default.jpg")).readAllBytes();
            user.setPhotoBlob(defaultPhoto);
        } catch (IOException e) {
            throw new RuntimeException("Default photo not found", e);
        }
        */
        /*
         * Instead, have the DB return null and send the default photo
         * this will allow us to not have to store the default photo in the DB multiple times
         * and also allows us to not have to check if the photo is null every time we
         * want to display the user photo.
         */


        User saved = userRepository.save(user);

        // Log user creation with phone number if provided
        String logDetails = "User created: " + saved.getEmail();
        if (saved.getPhoneNumber() != null && !saved.getPhoneNumber().trim().isEmpty()) {
            logDetails += " with phone number: " + saved.getPhoneNumber();
        }
        activityLogService.logActivity("CREATE", "USER", saved.getId(), logDetails);

        return saved;
    }
    // READ
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    public List<User> getInactiveUsers() {
        return userRepository.findByIsActiveFalse();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // UPDATE
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        // Check if phone number is being changed
        boolean phoneNumberChanged = !Objects.equals(existingUser.getPhoneNumber(), updatedUser.getPhoneNumber());
        String oldPhoneNumber = existingUser.getPhoneNumber();
        String newPhoneNumber = updatedUser.getPhoneNumber();

        User savedUser = userRepository.findById(id)
                .map(user -> {
                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());
                    user.setPhoneNumber(updatedUser.getPhoneNumber());

                    if (updatedUser.getPasswordHash() != null) {
                        if (!updatedUser.getPasswordHash().startsWith("$2a$")) {
                            user.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
                        } else {
                            // Already hashed, set as-is
                            user.setPasswordHash(updatedUser.getPasswordHash());
                        }
                    }

                    return userRepository.save(user);
                })
                .orElseThrow(UserNotFoundException::new);

        // Log the update activity
        String logDetails = "User updated: " + savedUser.getEmail();

        // If phone number changed, log it specifically
        if (phoneNumberChanged) {
            if (oldPhoneNumber == null) {
                logDetails += " - Phone number added: " + newPhoneNumber;
            } else if (newPhoneNumber == null) {
                logDetails += " - Phone number removed (was: " + oldPhoneNumber + ")";
            } else {
                logDetails += " - Phone number changed from " + oldPhoneNumber + " to " + newPhoneNumber;
            }
        }

        activityLogService.logActivity("UPDATE", "USER", savedUser.getId(), logDetails);
        return savedUser;
    }

    // DELETE
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        userRepository.deleteById(id);
        activityLogService.logActivity("DELETE", "USER", user.getId(), "User deleted: " + user.getEmail());
    }

    // Şifre kontrol
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public boolean isUserActive(Long userId) {
        return userRepository.findById(userId)
                .map(User::getIsActive)
                .orElse(false);
    }

    @Transactional
    public User setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.setIsActive(active);
        User saved = userRepository.save(user);
        activityLogService.logActivity("STATUS_UPDATE", "USER", userId, "User active status set to: " + active);
        return saved;
    }

    public void logUserLogin(Long userId, String email) {
        // Before: activityLogService.logActivity("LOGIN", "USER", userId, "User login: " + email);
        activityLogService.logUserActivity(userId, "LOGIN", "USER", userId, "User login: " + email);
    }

    public void logUserLogout(Long userId, String email) {
        // Before: activityLogService.logActivity("LOGOUT", "USER", userId, "User logout: " + email);
        activityLogService.logUserActivity(userId, "LOGOUT", "USER", userId, "User logout: " + email);
    }

    // FOTOĞRAF UPLOAD
    @Transactional
    public void uploadUserPhoto(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.setPhotoBlob(file.getBytes());
        userRepository.save(user);

        activityLogService.logUserActivity(userId, "UPDATE", "USER", userId, "User photo updated");
    }

    // FOTOĞRAF GETİRME
    public byte[] loadUserPhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getPhotoBlob() == null) {
            try {
                return Objects.requireNonNull(getClass().getResourceAsStream("/img/default.jpg")).readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Default photo not found", e);
            }
        }

        return user.getPhotoBlob();
    }

    // FOTOĞRAF SİLME
    @Transactional
    public void deleteUserPhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getPhotoBlob() == null) {
            throw new UserPhotoNotFoundException("Kullanıcı fotoğrafı yok");
        }

        user.setPhotoBlob(null);
        userRepository.save(user);

        activityLogService.logUserActivity(userId, "DELETE", "USER", userId, "User photo deleted");
    }
    //İlk admin hesabı için geçici şifre


    public User createUserWithTemporaryPassword(User user, String temporaryPassword) {


        // Hash the temporary password


        String hashedPassword = passwordEncoder.encode(temporaryPassword);
        // Hash the temporary password
        user.setPassword(hashedPassword);

        // Set creation date
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        User savedUser = userRepository.save(user);

        // Log the activity
        activityLogService.logActivity("CREATE", "USER", savedUser.getId(), "Admin user created with temporary password: " + savedUser.getEmail());

        return savedUser;
    }

    // NEW METHOD: Update phone number only
    @Transactional
    public User updatePhoneNumber(Long userId, String newPhoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String oldPhoneNumber = user.getPhoneNumber();

        // Check if phone number is actually changing
        if (Objects.equals(oldPhoneNumber, newPhoneNumber)) {
            // No change, return user as-is
            return user;
        }

        user.setPhoneNumber(newPhoneNumber);
        User savedUser = userRepository.save(user);

        // Log the phone number change
        String logDetails;
        if (oldPhoneNumber == null) {
            logDetails = "Phone number added: " + newPhoneNumber;
        } else if (newPhoneNumber == null) {
            logDetails = "Phone number removed (was: " + oldPhoneNumber + ")";
        } else {
            logDetails = "Phone number changed from " + oldPhoneNumber + " to " + newPhoneNumber;
        }

        activityLogService.logActivity("PHONE_UPDATE", "USER", userId, logDetails);

        return savedUser;
    }

    public boolean userHasRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName().equalsIgnoreCase(roleName));
    }
}
