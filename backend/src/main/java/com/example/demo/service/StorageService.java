package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {
    /**
     * Kullanıcının fotoğrafını saklar ve DB'de tutulacak relative path'i/dosya adını döner.
     * Örn: uploads/userimg/{userId}/user_{userId}_{uuid}.jpg
     */
    String storeUserPhoto(Long userId, MultipartFile file) throws IOException;

    /**
     * Relative path veya dosya adı üzerinden kullanıcı fotoğrafını siler.
     */
    void deleteUserPhoto(String fileNameOrRelativePath) throws IOException;

    /**
     * DB’de tutulan relative path/dosya adı -> gerçek dosya sistemindeki mutlak Path.
     */
    Path getUserPhotoPath(String fileNameOrRelativePath);

    /**
     * Verilen Path’teki dosyayı byte[] olarak okur.
     */
    byte[] loadFileAsBytes(Path absolutePath) throws IOException;
}
