package com.familymoney.domains.users.controllers.mappers;

import com.familymoney.domains.users.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.users.services.data.UserData;

public final class GetMyUserResponseMapper {

  private GetMyUserResponseMapper() {
    /* this class is not meant to be instantiated */
  }

  public static GetMyUserResponseDto toDto(final UserData userData) {
    return new GetMyUserResponseDto(
        userData.username().value(), userData.email().value(), userData.createdAt());
  }
}
