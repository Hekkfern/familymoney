package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;

public final class UpdateGroupDataMapper {

  private UpdateGroupDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateGroupDto toDbo(final UpdateGroupData data) {
    return new UpdateGroupDto(data.name(), data.description());
  }
}
