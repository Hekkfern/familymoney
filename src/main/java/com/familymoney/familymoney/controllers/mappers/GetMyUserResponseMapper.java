package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.services.data.GetUserData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GetMyUserResponseMapper {

  @Mapping(target = "username", expression = "java(userData.username().toString())")
  @Mapping(target = "email", expression = "java(userData.email().toString())")
  GetMyUserResponseDto toDto(GetUserData userData);
}
