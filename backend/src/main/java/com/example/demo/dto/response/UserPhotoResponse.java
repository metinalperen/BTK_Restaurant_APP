package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPhotoResponse {

    @Schema(description = "Yüklenen fotoğrafın sistemde kayıtlı dosya adı",
            example = "user_15_20250808_143215.jpg")
    private String fileName;
}
