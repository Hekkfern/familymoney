package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.auth.RefreshResponseDto;
import com.familymoney.familymoney.services.data.TokenPair;
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
