package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.GroupName;
import org.springframework.stereotype.Component;

@Component
public class UpdateGroupRequestMapper {

  public UpdateGroupData fromDto(UpdateGroupRequestDto dto) {
    return UpdateGroupData.builder()
        .name(dto.name() != null ? GroupName.fromString(dto.name()) : null)
        .description(dto.description() != null ? dto.description() : null)
        .build();
  }
}
