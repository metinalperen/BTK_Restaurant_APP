package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalonRequestDTO {

    @Schema(description = "Salon adı", example = "Ana Salon", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Salon adı boş olamaz")
    @Size(min = 1, max = 100, message = "Salon adı 1-100 karakter arasında olmalıdır")
    private String name;

    @Schema(description = "Salon açıklaması", example = "Restoran ana salonu, deniz manzaralı")
    @Size(max = 300, message = "Salon açıklaması en fazla 500 karakter olabilir")
    private String description;

    @Schema(description = "Salon kapasitesi (opsiyonel; tablolar üzerinden hesaplanır)", example = "50", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer capacity;
}
