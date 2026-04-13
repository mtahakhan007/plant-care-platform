package com.example.plant_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantRequest {

    private String name;
    private String type;
    private String category;
}
