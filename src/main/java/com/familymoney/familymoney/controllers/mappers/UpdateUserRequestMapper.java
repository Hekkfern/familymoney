package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.services.data.UpdateUserData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UpdateUserRequestMapper {

  UpdateUserData fromDto(UpdateUserRequestDto dto);
}
