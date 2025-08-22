package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableStatusRequestDto {

    @NotBlank(message = "Masa durumu adı boş olamaz.")
    @Size(min = 2, max = 50, message = "Masa durumu adı 2 ile 50 karakter arasında olmalıdır.")
    private String name;
}


