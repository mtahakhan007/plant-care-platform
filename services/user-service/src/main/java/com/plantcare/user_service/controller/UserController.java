package com.plantcare.user_service.controller;

import com.plantcare.user_service.dto.UserRequest;
import com.plantcare.user_service.dto.UserResponse;
import com.plantcare.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody @Valid UserRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public List<UserResponse> getAll( @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {

        return userService.getAll(page, pageSize);
    }

    @GetMapping("/getmyprofile")
    public UserResponse getMyProfile(Authentication authentication){
        String email = authentication.getName();
        return userService.getByEmail(email);
    }
}
