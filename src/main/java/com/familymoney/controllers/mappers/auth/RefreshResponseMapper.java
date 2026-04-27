package com.familymoney.controllers.mappers.auth;

import com.familymoney.controllers.dtos.auth.RefreshResponseDto;
import com.familymoney.services.data.TokenPair;
import org.springframework.stereotype.Component;

@Component
public class RefreshResponseMapper {

  public RefreshResponseDto toDto(TokenPair tokenPair) {
    return RefreshResponseDto.builder()
        .accessToken(tokenPair.accessToken().value())
        .refreshToken(tokenPair.refreshToken().value())
        .build();
  }
}
