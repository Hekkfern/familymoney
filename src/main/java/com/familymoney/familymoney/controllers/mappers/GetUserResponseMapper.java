package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.services.data.GetUserData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GetUserResponseMapper {

  @Mapping(target = "username", expression = "java(userData.username().toString())")
  @Mapping(target = "email", expression = "java(userData.email().toString())")
  GetUserResponseDto toDto(GetUserData userData);
}
