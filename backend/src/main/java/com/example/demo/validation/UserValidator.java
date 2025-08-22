package com.example.demo.validation;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserValidator {

    private final UserService userService;

    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    public List<String> validateUser(UserRequestDTO userRequestDTO) {
        List<String> errors = new ArrayList<>();

        // Boş alan kontrolleri
        if (userRequestDTO.getName() == null || userRequestDTO.getName().trim().isEmpty()) {
            errors.add("İsim boş olamaz");
        }

        if (userRequestDTO.getEmail() == null || userRequestDTO.getEmail().trim().isEmpty()) {
            errors.add("Email boş olamaz");
        }

        if (userRequestDTO.getPassword() == null || userRequestDTO.getPassword().trim().isEmpty()) {
            errors.add("Şifre boş olamaz");
        }

        // Email format kontrolü
        if (userRequestDTO.getEmail() != null && !isValidEmail(userRequestDTO.getEmail())) {
            errors.add("Geçerli bir email adresi giriniz");
        }

        // Email benzersizlik kontrolü (sadece yeni kayıt için)
        if (userRequestDTO.getEmail() != null && isValidEmail(userRequestDTO.getEmail())) {
            boolean emailExists = userService.getUserByEmail(userRequestDTO.getEmail()).isPresent();
            if (emailExists) {
                errors.add("Bu email adresi zaten kullanılıyor");
            }
        }

        // İsim kontrolü
        if (userRequestDTO.getName() != null && !userRequestDTO.getName().trim().isEmpty()) {
            if (userRequestDTO.getName().trim().length() < 2) {
                errors.add("İsim en az 2 karakter olmalıdır");
            }

            if (!userRequestDTO.getName().matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
                errors.add("İsim sadece harf içerebilir");
            }
        }

        // Şifre güvenlik kontrolü
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().trim().isEmpty()) {
            if (!isStrongPassword(userRequestDTO.getPassword())) {
                errors.add("Şifre en az 6 karakter olmalı ve en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter (@$!%*?&) içermelidir");
            }
        }
        //rol kontrolü
        if (userRequestDTO.getRoleName() == null || userRequestDTO.getRoleName().trim().isEmpty()) {
            errors.add("Rol boş olamaz");
        } else if (!isValidRole(userRequestDTO.getRoleName())) {
            errors.add("Geçerli bir rol seçiniz: admin, waiter, cashier");
        }

        return errors;
    }

    public List<String> validateUserUpdate(UserRequestDTO userRequestDTO, Long userId) {
        List<String> errors = new ArrayList<>();

        // Boş alan kontrolleri
        if (userRequestDTO.getName() == null || userRequestDTO.getName().trim().isEmpty()) {
            errors.add("İsim boş olamaz");
        }

        if (userRequestDTO.getEmail() == null || userRequestDTO.getEmail().trim().isEmpty()) {
            errors.add("Email boş olamaz");
        }

        // Email format kontrolü
        if (userRequestDTO.getEmail() != null && !isValidEmail(userRequestDTO.getEmail())) {
            errors.add("Geçerli bir email adresi giriniz");
        }

        // Email benzersizlik kontrolü (güncelleme için - kendi email'ini hariç tut)
        if (userRequestDTO.getEmail() != null && isValidEmail(userRequestDTO.getEmail())) {
            var existingUser = userService.getUserByEmail(userRequestDTO.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                errors.add("Bu email adresi zaten kullanılıyor");
            }
        }

        // İsim kontrolü
        if (userRequestDTO.getName() != null && !userRequestDTO.getName().trim().isEmpty()) {
            if (userRequestDTO.getName().trim().length() < 2) {
                errors.add("İsim en az 2 karakter olmalıdır");
            }

            if (!userRequestDTO.getName().matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
                errors.add("İsim sadece harf içerebilir");
            }
        }

        // Şifre güvenlik kontrolü (opsiyonel güncelleme)
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().trim().isEmpty()) {
            if (!isStrongPassword(userRequestDTO.getPassword())) {
                errors.add("Şifre en az 6 karakter olmalı ve en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter (@$!%*?&) içermelidir");
            }
        }

        return errors;
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public boolean isStrongPassword(String password) {
        return password != null &&
               password.length() >= 6 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*\\d.*") &&
               password.matches(".*[@$!%*?&].*");
        }

    private boolean isValidRole(String roleName) {
        return roleName != null && roleName.matches("^(admin|waiter|cashier)$");
    }

} 