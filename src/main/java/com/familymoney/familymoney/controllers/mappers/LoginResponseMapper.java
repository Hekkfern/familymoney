package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.auth.LoginResponseDto;
import com.familymoney.familymoney.services.data.TokenPair;
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
