package com.familymoney.controllers.mappers.user;

import com.familymoney.controllers.dtos.user.GetUserResponseDto;
import com.familymoney.services.data.UserData;
import org.springframework.stereotype.Component;

@Component
public class GetUserResponseMapper {

  public GetUserResponseDto toDto(UserData userData) {
    return GetUserResponseDto.builder()
        .id(userData.id().value())
        .username(userData.username().value())
        .email(userData.email().value())
        .createdAt(userData.createdAt())
        .isEnabled(userData.isEnabled())
        .build();
  }
}
