package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.services.data.GroupData;

public final class GetGroupResponseMapper {

  private GetGroupResponseMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GetGroupResponseDto toDto(final GroupData groupData) {
    return new GetGroupResponseDto(
        groupData.id().value(),
        groupData.name().value(),
        groupData.description().value(),
        groupData.currency().getCurrencyCode(),
        groupData.createdAt());
  }
}
