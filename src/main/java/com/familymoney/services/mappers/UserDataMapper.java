package com.familymoney.services.mappers;

import com.familymoney.repositories.entities.UserEntity;
import com.familymoney.services.data.UserData;

public class UserDataMapper {

  private UserDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UserData fromDbo(UserEntity entity) {
    return UserData.builder()
        .id(entity.id())
        .username(entity.username())
        .email(entity.email())
        .createdAt(entity.createdAt())
        .isEmailVerified(entity.isEmailVerified())
        .isEnabled(entity.isEnabled())
        .build();
  }
}
