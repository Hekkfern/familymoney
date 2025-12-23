package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.services.data.GetUserData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GetMyUserResponseMapper {

  GetMyUserResponseDto toDto(GetUserData userData);
}
