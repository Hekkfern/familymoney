package com.familymoney.domains.auth.controllers.mappers;

import com.familymoney.domains.auth.controllers.dtos.LoginResponseDto;
import com.familymoney.domains.auth.services.data.TokenPair;

public final class LoginResponseMapper {

  private LoginResponseMapper() {
    /* this class is not meant to be instantiated */
  }

  public static LoginResponseDto toDto(final TokenPair tokenPair) {
    return new LoginResponseDto(tokenPair.accessToken().value(), tokenPair.refreshToken().value());
  }
}
