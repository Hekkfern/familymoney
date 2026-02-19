package com.familymoney.familymoney.services.mappers;

import com.familymoney.familymoney.repositories.dtos.UpdateGroupDto;
import com.familymoney.familymoney.services.data.UpdateGroupData;

public class UpdateGroupDataMapper {

  private UpdateGroupDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static UpdateGroupDto toDbo(UpdateGroupData data) {
    return UpdateGroupDto.builder().name(data.getName()).description(data.getDescription()).build();
  }
}
