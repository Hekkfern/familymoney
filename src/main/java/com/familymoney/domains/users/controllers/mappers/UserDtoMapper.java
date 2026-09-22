package com.familymoney.domains.users.controllers.mappers;

import com.familymoney.domains.users.controllers.dtos.UserDto;
import com.familymoney.domains.users.services.data.UserData;

public final class UserDtoMapper {

  private UserDtoMapper() {
    /* this class is not meant to be instantiated */
  }

  public static UserDto toDto(final UserData userData) {
    return new UserDto(
        userData.id().value(),
        userData.username().value(),
        userData.email().value(),
        userData.createdAt(),
        userData.isEmailVerified(),
        userData.isEnabled());
  }
}
