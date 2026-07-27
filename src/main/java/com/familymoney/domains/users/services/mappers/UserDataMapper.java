package com.familymoney.domains.users.services.mappers;

import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.services.data.UserData;

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
