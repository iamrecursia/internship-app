package com.kozitskiy.authservice.controller;

import com.kozitskiy.authservice.dto.JwtResponse;
import com.kozitskiy.authservice.dto.LoginRequest;
import com.kozitskiy.authservice.dto.RegisterRequest;
import com.kozitskiy.authservice.service.AuthService;
import com.kozitskiy.authservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody Map<String, String> req){
        String refreshToken = req.get("refresh_token");
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        boolean valid = jwtUtil.validate(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}