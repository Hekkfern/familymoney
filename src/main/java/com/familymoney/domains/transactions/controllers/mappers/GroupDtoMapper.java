package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.services.data.GroupData;

public final class GroupDtoMapper {

  private GroupDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GroupDto toDto(final GroupData groupData) {
    return new GroupDto(
        groupData.id().value(),
        groupData.name().value(),
        groupData.description().value(),
        groupData.currency().getCurrencyCode(),
        groupData.createdAt());
  }
}
