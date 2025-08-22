package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalDiskStorageService implements StorageService {

    @Value("${app.storage.user-photos}")
    private String rootPath; // örn: uploads/userimg

    @Override
    public String storeUserPhoto(Long userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Dosya boş");

        // İçerik tipine göre uzantı
        String ext = switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Desteklenmeyen dosya tipi");
        };

        // Kullanıcıya özel klasör
        Path userDir = Path.of(rootPath, String.valueOf(userId));
        Files.createDirectories(userDir);

        // Benzersiz dosya adı
        String fileName = "user_" + userId + "_" + UUID.randomUUID() + ext;
        Path target = userDir.resolve(fileName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // DB'ye relative path kaydediyoruz
        return String.valueOf(userId) + "/" + fileName;
    }

    @Override
    public void deleteUserPhoto(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) return;
        Path filePath = getUserPhotoPath(relativePath);
        if (Files.exists(filePath)) Files.delete(filePath);
    }

    @Override
    public Path getUserPhotoPath(String relativePath) {
        // Relative path → Mutlak path
        return Path.of(rootPath).resolve(relativePath).toAbsolutePath().normalize();
    }

    @Override
    public byte[] loadFileAsBytes(Path absolutePath) throws IOException {
        return Files.readAllBytes(absolutePath);
    }
}
