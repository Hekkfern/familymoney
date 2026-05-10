package com.familymoney.domains.auth.controllers.mappers;

import com.familymoney.domains.auth.controllers.dtos.RefreshResponseDto;
import com.familymoney.domains.auth.services.data.TokenPair;
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
