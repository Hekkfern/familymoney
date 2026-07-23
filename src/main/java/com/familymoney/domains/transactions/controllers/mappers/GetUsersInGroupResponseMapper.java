package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.user.types.UserId;
import java.util.List;
import lombok.val;

public final class GetUsersInGroupResponseMapper {

  private GetUsersInGroupResponseMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GetUsersInGroupResponseDto toDto(final List<UserId> users) {
    val userIds = users.stream().map(UserId::value).toList();
    return new GetUsersInGroupResponseDto(userIds);
  }
}
