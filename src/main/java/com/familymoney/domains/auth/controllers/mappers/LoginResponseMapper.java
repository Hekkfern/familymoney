package com.familymoney.domains.auth.controllers.mappers;

import com.familymoney.domains.auth.controllers.dtos.LoginResponseDto;
import com.familymoney.domains.auth.services.data.TokenPair;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseMapper {

  public LoginResponseDto toDto(TokenPair tokenPair) {
    return LoginResponseDto.builder()
        .accessToken(tokenPair.accessToken().value())
        .refreshToken(tokenPair.refreshToken().value())
        .build();
  }
}
