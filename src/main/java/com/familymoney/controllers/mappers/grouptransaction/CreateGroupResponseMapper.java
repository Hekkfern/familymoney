package com.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.controllers.dtos.grouptransaction.CreateGroupResponseDto;
import com.familymoney.types.GroupId;
import org.springframework.stereotype.Component;

@Component
public class CreateGroupResponseMapper {

  public CreateGroupResponseDto toDto(GroupId groupId) {
    return CreateGroupResponseDto.builder().id(groupId.value()).build();
  }
}
