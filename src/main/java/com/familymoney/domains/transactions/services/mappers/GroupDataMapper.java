package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.services.data.GroupData;

public final class GroupDataMapper {

  private GroupDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static GroupData fromDbo(final GroupEntity entity) {
    return new GroupData(
        entity.id(), entity.name(), entity.description(), entity.currency(), entity.createdAt());
  }
}
