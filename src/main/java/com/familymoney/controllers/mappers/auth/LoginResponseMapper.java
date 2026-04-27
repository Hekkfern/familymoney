package com.familymoney.controllers.mappers.auth;

import com.familymoney.controllers.dtos.auth.LoginResponseDto;
import com.familymoney.services.data.TokenPair;
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
