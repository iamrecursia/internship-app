package com.kozitskiy.authservice.controller;

import com.kozitskiy.authservice.client.UserClient;
import com.kozitskiy.authservice.dto.CreateUserRequest;
import com.kozitskiy.authservice.dto.LoginRequest;
import com.kozitskiy.authservice.dto.RegisterRequest;
import com.kozitskiy.authservice.entity.User;
import com.kozitskiy.authservice.entity.UserRole;
import com.kozitskiy.authservice.exception.EmailAlreadyExistsException;
import com.kozitskiy.authservice.exception.RegistrationFailedException;
import com.kozitskiy.authservice.exception.UserServiceUnavailableException;
import com.kozitskiy.authservice.repository.UserRepository;
import com.kozitskiy.authservice.util.JwtUtil;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserClient userClient;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Long userId = null;

        try {
            User authUser = new User();
            authUser.setEmail(request.getEmail());
            authUser.setPassword(passwordEncoder.encode(request.getPassword()));
            authUser.setRole(UserRole.USER);

            User savedUser = userRepository.save(authUser);
            userId = savedUser.getId();

            CreateUserRequest userRequest = CreateUserRequest.builder()
                    .name(request.getName())
                    .surname(request.getSurname())
                    .birthDate(request.getBirthDate())
                    .email(request.getEmail())
                    .build();

            userClient.createUser(userRequest);
            return "User registered successfully";

        }catch (UserServiceUnavailableException | FeignException | ResourceAccessException e){
            if (userId != null){
                try {
                    userRepository.deleteById(userId);
                } catch (Exception deleteEx) {
                    logger.error("Failed to rollback user registration for email: {}", request.getEmail(), deleteEx);
                }
            }
            throw new RegistrationFailedException("Registration failed: ", e);
        }

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