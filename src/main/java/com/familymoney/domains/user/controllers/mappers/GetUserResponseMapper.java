package com.familymoney.domains.user.controllers.mappers;

import com.familymoney.domains.user.controllers.dtos.GetUserResponseDto;
import com.familymoney.domains.user.services.data.UserData;

public final class GetUserResponseMapper {

  private GetUserResponseMapper() {
    /* this class is not meant to be instantiated */
  }

  public static GetUserResponseDto toDto(final UserData userData) {
    return new GetUserResponseDto(
        userData.id().value(),
        userData.username().value(),
        userData.email().value(),
        userData.createdAt(),
        userData.isEmailVerified(),
        userData.isEnabled());
  }
}
