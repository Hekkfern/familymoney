package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.services.data.GetUserData;
import org.springframework.stereotype.Component;

@Component
public class GetMyUserResponseMapper {

  public GetMyUserResponseDto toDto(GetUserData userData) {
    return GetMyUserResponseDto.builder()
        .username(userData.username().value())
        .email(userData.email().value())
        .createdAt(userData.createdAt())
        .build();
  }
}
