package com.kozitskiy.userservice.it;

import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private final String BASE_URL = "/api/v1/users";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save user to database and return 201")
    void createUser_ShouldSaveToDb() {
        UserRequest request = new UserRequest("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");

        ResponseEntity<UserResponse> response = restTemplate.postForEntity(BASE_URL, request, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("john@example.com");
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Should return user from database by ID")
    void getUserById_ShouldReturnStoredUser() {
        UserRequest request = new UserRequest("Alice", "Smith", LocalDate.of(1995, 5, 5), "alice@example.com");
        Long id = restTemplate.postForEntity(BASE_URL, request, UserResponse.class).getBody().id();

        ResponseEntity<UserResponse> response = restTemplate.getForEntity(BASE_URL + "/" + id, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should update user data in database")
    void updateUser_ShouldUpdateDbRecord() {
        UserRequest initialRequest = new UserRequest("Old", "Name", LocalDate.of(1980, 1, 1), "old@example.com");
        Long id = restTemplate.postForEntity(BASE_URL, initialRequest, UserResponse.class).getBody().id();

        UserRequest updateRequest = new UserRequest("New", "Name", LocalDate.of(1980, 1, 1), "new@example.com");

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                BASE_URL + "/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("New");
        assertThat(userRepository.findById(id).get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Should delete user from database")
    void deleteUser_ShouldRemoveFromDb() {
        UserRequest request = new UserRequest("To", "Delete", LocalDate.of(2000, 1, 1), "delete@example.com");
        Long id = restTemplate.postForEntity(BASE_URL, request, UserResponse.class).getBody().id();

        restTemplate.delete(BASE_URL + "/" + id);

        assertThat(userRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should return 400 when creating user with invalid email")
    void createUser_InvalidEmail_ShouldReturn400() {
        UserRequest invalidRequest = new UserRequest("John", "Doe", LocalDate.now(), "not-an-email");

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, invalidRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}