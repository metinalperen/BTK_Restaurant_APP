package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalonResponseDTO{

    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    private int totalTables;
    private int occupiedTables;
    private double occupancyRate; // yüzde
}
