package com.example.userservicemar24.controllers;

import com.example.userservicemar24.dtos.*;
import com.example.userservicemar24.models.SessionStatus;
import com.example.userservicemar24.services.AuthService;
import org.springframework.http.HttpStatus;
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
        return authService.login(requestDto.getEmail(),requestDto.getPassword());
        //return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignUpRequestDto requestDto) {
        UserDto userDto = authService.signup(requestDto.getEmail(),requestDto.getPassword());
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto request) {
        return authService.logout(request.getToken(), request.getUserId());
    }

    @PostMapping("/validate")
    public SessionStatus validate(@RequestBody ValidateTokenRequestDto request){
        return authService.validate(request.getToken(),request.getUserId());
    }


}
