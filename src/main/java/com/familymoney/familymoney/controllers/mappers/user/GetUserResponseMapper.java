package com.familymoney.familymoney.controllers.mappers.user;

import com.familymoney.familymoney.controllers.dtos.user.GetUserResponseDto;
import com.familymoney.familymoney.services.data.GetUserData;
import org.springframework.stereotype.Component;

@Component
public class GetUserResponseMapper {

  public GetUserResponseDto toDto(GetUserData userData) {
    return GetUserResponseDto.builder()
        .id(userData.id().value())
        .username(userData.username().value())
        .email(userData.email().value())
        .createdAt(userData.createdAt())
        .isEnabled(userData.isEnabled())
        .build();
  }
}
