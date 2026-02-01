package com.kozitskiy.userservice.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.dto.UserWithCardResponse;
import com.kozitskiy.userservice.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final String BASE_URL = "/api/v1/users";

    @Test
    @DisplayName("Should return 201 Created when creating a valid user")
    void createUser_ShouldReturnCreated() throws Exception {

        UserRequest request = new UserRequest("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");
        UserResponse response = new UserResponse(1L, "John", "Doe", "john@example.com", LocalDate.of(1990, 1, 1));

        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("Should return 200 OK when updating a user")
    void updateUser_ShouldReturnOk() throws Exception {

        long id = 1L;
        UserRequest request = new UserRequest("Jane", "Doe", LocalDate.of(1995, 5, 5), "jane@example.com");
        UserResponse response = new UserResponse(id, "Jane", "Doe", "jane@example.com", LocalDate.of(1995, 5, 5));

        when(userService.updateUserById(eq(id), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    @DisplayName("Should return user by ID")
    void getUserById_ShouldReturnUser() throws Exception {

        long id = 1L;
        UserResponse response = new UserResponse(id, "John", "Doe", "john@example.com", LocalDate.of(1990, 1, 1));

        when(userService.getUserById(id)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    @DisplayName("Should return all users")
    void getAllUsers_ShouldReturnList() throws Exception {

        UserResponse user = new UserResponse(1L, "John", "Doe", "john@example.com", LocalDate.of(1990, 1, 1));
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("John"));
    }

    @Test
    @DisplayName("Should return 204 No Content on deletion")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        long id = 1L;
        doNothing().when(userService).deleteUserById(id);

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return user by email via search")
    void getUserByEmail_ShouldReturnUser() throws Exception {
        String email = "search@example.com";
        UserResponse response = new UserResponse(1L, "Search", "User", email, LocalDate.of(2000, 1, 1));

        when(userService.getUserByEmail(email)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/search")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("Should return user with cards")
    void getUserWithCards_ShouldReturnCombinedResponse() throws Exception {
        long id = 1L;
        UserWithCardResponse response = UserWithCardResponse.builder()
                .id(id)
                .name("John")
                .cards(Collections.emptyList())
                .build();

        when(userService.getUserWithCards(id)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}/with-cards", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.cards").isArray());
    }

//    @Test
//    @DisplayName("Should return 400 Bad Request when validation fails")
//    void createUser_InvalidRequest_ShouldReturnBadRequest() throws Exception {
//        UserRequest invalidRequest = new UserRequest("", "Doe", LocalDate.now(), "not-an-email");
//
//        mockMvc.perform(post(BASE_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andExpect(status().isBadRequest());
//    }
}