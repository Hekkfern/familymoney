package com.familymoney.domains.user.controllers.mappers;

import com.familymoney.domains.user.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.user.services.data.UserData;
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
