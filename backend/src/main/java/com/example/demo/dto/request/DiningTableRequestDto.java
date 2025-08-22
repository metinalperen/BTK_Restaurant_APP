package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class DiningTableRequestDto {

    @Schema(description = "Masa numarası", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Masa numarası boş olamaz")
    @Min(value = 1, message = "Masa numarası 1'den küçük olamaz")
    private Integer tableNumber;

    @Schema(description = "Masa durumu ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Masa durumu ID boş olamaz")
    private Long statusId;

    @Schema(description = "Kapasite", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Kapasite boş olamaz")
    @Min(value = 1, message = "Kapasite 1'den küçük olamaz")
    private Integer capacity;

    @Schema(description = "Salon ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Salon ID boş olamaz")
    @Min(value = 1, message = "Salon ID 1'den küçük olamaz")
    private Long salonId;
}
