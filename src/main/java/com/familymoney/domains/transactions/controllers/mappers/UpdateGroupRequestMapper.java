package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupName;

public final class UpdateGroupRequestMapper {

  private UpdateGroupRequestMapper() {
    /* this class is not intended to be instantiated */
  }

  public static UpdateGroupData fromDto(final UpdateGroupRequestDto dto) {
    return new UpdateGroupData(
        dto.name() != null ? GroupName.fromString(dto.name()) : null,
        dto.description() != null ? Description.of(dto.description()) : null);
  }
}
