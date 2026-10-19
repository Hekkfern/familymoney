package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.services.data.MemberData;

public class MemberDtoMapper {

  private MemberDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  public static MemberDto toDto(final MemberData data) {
    return new MemberDto(data.userId().value(), data.username().value());
  }
}
