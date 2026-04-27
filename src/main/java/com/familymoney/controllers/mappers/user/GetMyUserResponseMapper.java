package com.familymoney.controllers.mappers.user;

import com.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.services.data.UserData;
import org.springframework.stereotype.Component;

@Component
public class GetMyUserResponseMapper {

  public GetMyUserResponseDto toDto(UserData userData) {
    return GetMyUserResponseDto.builder()
        .username(userData.username().value())
        .email(userData.email().value())
        .createdAt(userData.createdAt())
        .build();
  }
}
