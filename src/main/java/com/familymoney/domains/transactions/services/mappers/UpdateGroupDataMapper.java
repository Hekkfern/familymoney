package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;

public class UpdateGroupDataMapper {

  private UpdateGroupDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateGroupDto toDbo(UpdateGroupData data) {
    return UpdateGroupDto.builder().name(data.getName()).description(data.getDescription()).build();
  }
}
