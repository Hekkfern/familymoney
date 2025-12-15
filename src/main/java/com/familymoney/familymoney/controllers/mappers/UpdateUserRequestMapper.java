package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.user.UpdateUserRequestDto;
import com.familymoney.familymoney.services.data.UpdateUserData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpdateUserRequestMapper {

  UpdateUserData fromDto(UpdateUserRequestDto dto);
}
