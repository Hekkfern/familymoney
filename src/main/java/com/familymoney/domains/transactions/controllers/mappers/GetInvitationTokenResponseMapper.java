package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetInvitationTokenResponseDto;
import com.familymoney.domains.transactions.types.GroupInvitationToken;

public final class GetInvitationTokenResponseMapper {

  private GetInvitationTokenResponseMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GetInvitationTokenResponseDto toDto(final GroupInvitationToken token) {
    return new GetInvitationTokenResponseDto(token.value());
  }
}
