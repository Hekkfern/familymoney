package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.types.GroupId;

public final class CreateGroupResponseMapper {

  private CreateGroupResponseMapper() {
    /* this class is not intended to be instantiated */
  }

  public static CreateGroupResponseDto toDto(final GroupId groupId) {
    return new CreateGroupResponseDto(groupId.value());
  }
}
