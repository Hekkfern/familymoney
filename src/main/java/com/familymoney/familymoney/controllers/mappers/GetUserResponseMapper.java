package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.services.data.GetUserData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GetUserResponseMapper {

  GetUserResponseDto toDto(GetUserData userData);
}
