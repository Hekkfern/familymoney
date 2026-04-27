package com.familymoney.services.mappers;

import com.familymoney.repositories.entities.GroupEntity;
import com.familymoney.services.data.GroupData;

public class GroupDataMapper {

  private GroupDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static GroupData fromDbo(final GroupEntity entity) {
    return GroupData.builder()
        .id(entity.id())
        .name(entity.name())
        .description(entity.description())
        .currency(entity.currency())
        .createdAt(entity.createdAt())
        .build();
  }
}
