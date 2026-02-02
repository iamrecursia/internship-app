package com.kozitskiy.authservice.service;

import com.kozitskiy.authservice.client.UserClient;
import com.kozitskiy.authservice.dto.CreateUserRequest;
import com.kozitskiy.authservice.dto.JwtResponse;
import com.kozitskiy.authservice.dto.LoginRequest;
import com.kozitskiy.authservice.dto.RegisterRequest;
import com.kozitskiy.authservice.entity.User;
import com.kozitskiy.authservice.entity.UserRole;
import com.kozitskiy.authservice.exception.EmailAlreadyExistsException;
import com.kozitskiy.authservice.exception.RegistrationFailedException;
import com.kozitskiy.authservice.repository.UserRepository;
import com.kozitskiy.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;


    public void register(RegisterRequest request){
        if (userRepository.existsByEmail(request.email())){
            throw new EmailAlreadyExistsException("Email already in use: " + request.email());
        }

        User authUser = new User();
        authUser.setEmail(request.email());
        authUser.setPassword(passwordEncoder.encode(request.password()));
        authUser.setRole(UserRole.USER);

        User savedUser = userRepository.save(authUser);

        try {
            CreateUserRequest userRequest = CreateUserRequest.builder()
                    .name(request.name())
                    .surname(request.surname())
                    .birthDate(request.birthDate())
                    .email(request.email())
                    .build();

            userClient.createUser(userRequest);

            log.info("User registered successfully: {}", savedUser.getEmail());
        }catch (Exception e){

            log.error("Error creating user profile in User-Service. Rolling back Auth User id: {}", savedUser.getId());
            userRepository.deleteById(savedUser.getId());
            throw new RegistrationFailedException("Failed to create user profile. Registration rolled back.", e);
        }


    }

    public JwtResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.email())
        );

        String accessToken = jwtUtil.generateAccessToken(request.email());
        String refreshToken = jwtUtil.generateRefreshToken(request.email());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public JwtResponse refresh(String refreshToken){
        if(!jwtUtil.validate(refreshToken)){
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtUtil.getEmailFromToken(refreshToken);

        if (!userRepository.existsByEmail(email)){
            throw new RuntimeException("User not found");
        }

        String newAccessToken = jwtUtil.generateAccessToken(email);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }


}
