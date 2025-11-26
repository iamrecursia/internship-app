package com.kozitskiy.authservice.controller;

import com.kozitskiy.authservice.dto.LoginRequest;
import com.kozitskiy.authservice.dto.RegisterRequest;
import com.kozitskiy.authservice.entity.User;
import com.kozitskiy.authservice.entity.UserRole;
import com.kozitskiy.authservice.repository.UserRepository;
import com.kozitskiy.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest registerRequest) {
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        userRepository.save(user);
        return "User registered";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        //checks login & password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        return Map.of("access_token", accessToken,
                "refresh_token", refreshToken);
    }

    @GetMapping("/validate")
    public Map<String, Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        boolean valid = jwtUtil.validate(token);
        return Map.of("valid", valid);
    }

    @PostMapping("/refresh")
    public Map<String, String> refreshToken(@RequestBody Map<String, String> req) {
        String refreshToken = req.get("refresh_token");

        if (!jwtUtil.validate(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtUtil.getEmailFromToken(refreshToken);

        userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(email);
        return Map.of("access_token", newAccessToken);
    }
}