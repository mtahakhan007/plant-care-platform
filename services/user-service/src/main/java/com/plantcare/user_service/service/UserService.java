package com.plantcare.user_service.service;

import com.plantcare.user_service.dto.UserRequest;
import com.plantcare.user_service.dto.UserResponse;
import com.plantcare.user_service.entity.Role;
import com.plantcare.user_service.entity.User;
import com.plantcare.user_service.exception.EmailAlreadyExistsException;
import com.plantcare.user_service.exception.UserNotFoundException;
import com.plantcare.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponse(user);
    }

    public UserResponse getByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponse(user);
    }

    public List<UserResponse> getAll( int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize , Sort.by("id").ascending());
        return userRepository.findAll(pageable).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    // Utility Function used for converting Database data to DTO
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .build();
    }
}
