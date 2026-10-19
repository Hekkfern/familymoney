package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dbos.UpdateGroupDbo;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;

public final class UpdateGroupDataMapper {

  private UpdateGroupDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateGroupDbo toDbo(final UpdateGroupData data) {
    return new UpdateGroupDbo(data.name(), data.description());
  }
}
