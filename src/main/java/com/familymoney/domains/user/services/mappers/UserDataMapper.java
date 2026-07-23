package com.familymoney.domains.user.services.mappers;

import com.familymoney.domains.user.repositories.entitites.UserEntity;
import com.familymoney.domains.user.services.data.UserData;

public class UserDataMapper {

  private UserDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UserData fromDbo(final UserEntity entity) {
    return new UserData(
        entity.id(),
        entity.username(),
        entity.email(),
        entity.createdAt(),
        entity.isEmailVerified(),
        entity.isEnabled());
  }
}
