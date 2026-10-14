package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.users.services.data.UserData;

public class MemberDtoMapper {

  private MemberDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  public static MemberDto toDto(final UserData userData) {
    return new MemberDto(userData.id().value(), userData.username().value());
  }
}
