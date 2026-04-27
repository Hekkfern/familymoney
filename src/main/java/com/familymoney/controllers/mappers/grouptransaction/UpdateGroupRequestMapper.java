package com.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.controllers.dtos.grouptransaction.UpdateGroupRequestDto;
import com.familymoney.services.data.UpdateGroupData;
import com.familymoney.types.GroupName;
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
