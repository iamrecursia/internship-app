package com.kozitskiy.userservice.util;

import com.kozitskiy.userservice.dto.request.CreateUserDto;
import com.kozitskiy.userservice.dto.response.UserResponseDto;
import com.kozitskiy.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
   User toEntity(CreateUserDto dto);
   List<UserResponseDto> toDtoList(List<User> user);

   UserResponseDto toDto(User user);

   void updateFromDto(CreateUserDto dto, @MappingTarget User user);
}
