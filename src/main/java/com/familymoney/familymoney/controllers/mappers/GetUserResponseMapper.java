package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.services.data.GetUserData;
import org.springframework.stereotype.Component;

@Component
public class GetUserResponseMapper {

  public GetUserResponseDto toDto(GetUserData userData) {
    return GetUserResponseDto.builder()
        .id(userData.id().value())
        .username(userData.username().value())
        .createdAt(userData.createdAt())
        .isEnabled(userData.isEnabled())
        .build();
  }
}
