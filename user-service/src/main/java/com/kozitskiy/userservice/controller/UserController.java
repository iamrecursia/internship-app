package com.kozitskiy.userservice.controller;

import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.dto.UserWithCardResponse;
import com.kozitskiy.userservice.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable long id,
            @Valid @RequestBody UserRequest dto){
        return ResponseEntity.ok(userService.updateUserById(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id){
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam @Email String email){
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/{id}/with-cards")
    public ResponseEntity<UserWithCardResponse> getUserWithCards(@PathVariable long id){
        return ResponseEntity.ok(userService.getUserWithCards(id));
    }
}
