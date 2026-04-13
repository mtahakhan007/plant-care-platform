package com.example.plant_service.repository;

import com.example.plant_service.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByUserId(String userId);
}
