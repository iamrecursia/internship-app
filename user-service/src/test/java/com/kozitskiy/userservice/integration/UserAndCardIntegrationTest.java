package com.kozitskiy.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.userservice.dto.request.CreateCardDto;
import com.kozitskiy.userservice.dto.request.CreateUserDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import com.kozitskiy.userservice.dto.response.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.cache.type=NONE")
@AutoConfigureMockMvc
@Transactional
public class UserAndCardIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createTestUser() throws Exception {
        String uniqueEmail = "user+" + UUID.randomUUID() + "@example.com";

        CreateUserDto userDto = CreateUserDto.builder()
                .name("Test")
                .surname("User")
                .email(uniqueEmail)
                .birthDate(LocalDate.of(1990,1,1))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();


        UserResponseDto user = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDto.class
        );
        return user.getId();
    }

    private long createTestCard(long userId) throws Exception {

        CreateCardDto cardDto = CreateCardDto.builder()
                .userId(userId)
                .number("4576879689785647")
                .holder("User")
                .expirationDate(LocalDate.of(2026,12,31))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andReturn();

        CardResponseDto card = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CardResponseDto.class
        );
        return card.getId();
    }

    @Test
    void createUser_shouldReturn201_whenValidationProvided() throws Exception{
        CreateUserDto userDto = CreateUserDto.builder()
                .name("Kirill")
                .surname("Kozitkiy")
                .email("kozitskiy.2005@example.com")
                .birthDate(LocalDate.of(2005,12,26))
                .build();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Kirill"))
                .andExpect(jsonPath("$.email").value("kozitskiy.2005@example.com"));
    }

    @Test
    void createUser_shouldReturn400_whenEmailIsInvalid() throws Exception{
        CreateUserDto userDto = CreateUserDto.builder()
                .name("Kirill")
                .surname("Kozitkiy")
                .email("Invalid-email")
                .birthDate(LocalDate.of(2005,12,26))
                .build();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_shouldReturn200_whenUserExists() throws Exception {
        long userId = createTestUser();

        // Add this to see what's actually returned
        MvcResult result = mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response: " + responseContent); // Debug output

        // Then continue with your assertions
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Test"));
    }
    @Test
    void getUserByEmail_shouldReturn200_whenEmailExists() throws Exception{
        String email = "findme@example.com";

        CreateUserDto userDto = CreateUserDto.builder()
                .name("Kiril")
                .surname("Kozitsky")
                .email(email)
                .birthDate(LocalDate.of(2005,12,26))
                .build();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                        .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void deleteUser_shouldReturn204_andUserNotFoundAfterDeletion() throws Exception{
        long userId = createTestUser();

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_shouldReturn201_whenValidationProvided() throws Exception{
        long userId = createTestUser();

        CreateCardDto cardDto = CreateCardDto.builder()
                .userId(userId)
                .holder("Test holder")
                .number("3456789009876345")
                .expirationDate(LocalDate.of(2027,8,3))
                .build();

        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.holder").value("Test holder"));
    }

    @Test
    void createCard_shouldReturn400_whenCardNumberIsInvalid() throws Exception{
        long userId = createTestUser();

        CreateCardDto cardDto = CreateCardDto.builder()
                .userId(userId)
                .holder("Test holder")
                .number("34335")
                .expirationDate(LocalDate.of(2027,8,3))
                .build();

        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_shouldReturn200_whenCardExists() throws Exception{
        long userId = createTestUser();
        long cardId = createTestCard(userId);

        mockMvc.perform(get("/api/v1/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void updateCard_shouldReturn200_whenValidDataProvided() throws Exception{
        long userId = createTestUser();
        long cardId = createTestCard(userId);

        CreateCardDto updatedDto = CreateCardDto.builder()
                .userId(userId)
                .holder("Updated Holder")
                .number("4000000000000002")
                .expirationDate(LocalDate.of(2029, 12, 31))
                .build();

        mockMvc.perform(put("/api/v1/cards/{id}", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("Updated Holder"))
                .andExpect(jsonPath("$.number").value("4000000000000002"));
    }

    @Test
    void getAllCards_shouldReturnPageWithCards() throws Exception {
        long userId = createTestUser();
        createTestCard(userId);
        createTestCard(userId);

        mockMvc.perform(get("/api/v1/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getCardsByUserId_shouldReturnOnlyUsersCards() throws Exception {
        long user1 = createTestUser();
        long user2 = createTestUser();

        createTestCard(user1);
        createTestCard(user1);
        createTestCard(user2); // эта не должна попасть

        mockMvc.perform(get("/api/v1/cards/{userId}/by", user1)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].userId").value(user1));
    }

//    @Test
//    void getUserWithCards_shouldReturnUserAndTheirCards() throws Exception {
//        long userId = createTestUser();
//        createTestCard(userId);
//        createTestCard(userId);
//
//        mockMvc.perform(get("/api/v1/users/{id}/with-cards", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.user.id").value(userId))
//                .andExpect(jsonPath("$.cards").isArray())
//                .andExpect(jsonPath("$.cards.length()").value(2));
//    }




}
