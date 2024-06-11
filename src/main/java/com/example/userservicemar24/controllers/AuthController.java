package com.example.userservicemar24.controllers;

import com.example.userservicemar24.dtos.LoginRequestDto;
import com.example.userservicemar24.dtos.UserDto;
import com.example.userservicemar24.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto requestDto) {

        return null;
    }


}
