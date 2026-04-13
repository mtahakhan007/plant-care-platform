package com.example.plant_service.controller;

import com.example.plant_service.dto.PlantRequest;
import com.example.plant_service.dto.PlantResponse;
import com.example.plant_service.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlantResponse create(@RequestBody PlantRequest request, Authentication authentication) {
        String userId = authentication.getName(); // email from JWT subject
        return plantService.create(request, userId);
    }

    @GetMapping
    public List<PlantResponse> getAllPlant(Authentication authentication){
        String userId = authentication.getName();
        return plantService.getAll(userId);
    }
}
