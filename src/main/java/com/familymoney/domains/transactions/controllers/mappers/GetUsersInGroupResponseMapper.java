package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.users.types.UserId;
import java.util.List;
import java.util.UUID;

public final class GetUsersInGroupResponseMapper {

  private GetUsersInGroupResponseMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GetUsersInGroupResponseDto toDto(final List<UserId> users) {
    final List<UUID> userIds = users.stream().map(UserId::value).toList();
    return new GetUsersInGroupResponseDto(userIds);
  }
}
