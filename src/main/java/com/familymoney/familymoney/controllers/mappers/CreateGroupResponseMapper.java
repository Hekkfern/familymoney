package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.group.CreateGroupResponseDto;
import com.familymoney.familymoney.types.GroupId;
import org.springframework.stereotype.Component;

@Component
public class CreateGroupResponseMapper {

  public CreateGroupResponseDto toDto(GroupId groupId) {
    return CreateGroupResponseDto.builder().id(groupId.value()).build();
  }
}
