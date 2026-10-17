package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.users.repositories.entitites.UserEntity;

public final class MemberDataMapper {

  private MemberDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static MemberData fromDbo(final UserEntity entity) {
    return new MemberData(entity.id(), entity.username());
  }
}
