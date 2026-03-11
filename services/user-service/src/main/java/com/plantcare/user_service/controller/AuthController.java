package com.plantcare.user_service.controller;

import com.plantcare.user_service.dto.LoginRequest;
import com.plantcare.user_service.dto.LoginResponse;
import com.plantcare.user_service.security.JwtService;
import com.plantcare.user_service.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        String token = jwtService.generateToken(user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .build();
    }
}
