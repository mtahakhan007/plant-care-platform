package com.example.plant_service.service;

import com.example.plant_service.dto.PlantRequest;
import com.example.plant_service.dto.PlantResponse;
import com.example.plant_service.entity.Plant;
import com.example.plant_service.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;

    public PlantResponse create(PlantRequest request, String userId) {
        Plant plant = Plant.builder()
                .userId(userId)
                .name(request.getName())
                .type(request.getType())
                .category(request.getCategory())
                .build();
        plant = plantRepository.save(plant);
        return toResponse(plant);
    }

    public List<PlantResponse> getAll(String userId){
        List<Plant> plants = plantRepository.findByUserId(userId);
        return plants.stream()
                .map(this::toResponse)
                .toList();
    }

    private PlantResponse toResponse(Plant plant) {
        return PlantResponse.builder()
                .id(plant.getId())
//                .userId(plant.getUserId())
                .name(plant.getName())
                .type(plant.getType())
                .category(plant.getCategory())
                .build();
    }
}
