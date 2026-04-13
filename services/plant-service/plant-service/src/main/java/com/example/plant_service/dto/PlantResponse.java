package com.example.plant_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantResponse {

    private Long id;
//    private String userId;
    private String name;
    private String type;
    private String category;
}
