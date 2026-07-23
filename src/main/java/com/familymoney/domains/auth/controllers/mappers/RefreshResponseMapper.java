package com.familymoney.domains.auth.controllers.mappers;

import com.familymoney.domains.auth.controllers.dtos.RefreshResponseDto;
import com.familymoney.domains.auth.services.data.TokenPair;

public final class RefreshResponseMapper {

  private RefreshResponseMapper() {
    /* this class is not meant to be instantiated */
  }

  public static RefreshResponseDto toDto(final TokenPair tokenPair) {
    return new RefreshResponseDto(
        tokenPair.accessToken().value(), tokenPair.refreshToken().value());
  }
}
