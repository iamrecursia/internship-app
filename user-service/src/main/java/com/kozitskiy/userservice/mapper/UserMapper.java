package com.kozitskiy.userservice.mapper;

import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequest dto);

    List<UserResponse> toDtoList(List<User> user);

    UserResponse toDto(User user);

    void updateFromDto(UserRequest dto, @MappingTarget User user);
}
