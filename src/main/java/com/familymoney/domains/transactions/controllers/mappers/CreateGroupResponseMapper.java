package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.controllers.dtos.grouptransaction.CreateGroupResponseDto;
import com.familymoney.domains.transactions.types.GroupId;
import org.springframework.stereotype.Component;

@Component
public class CreateGroupResponseMapper {

  public CreateGroupResponseDto toDto(GroupId groupId) {
    return CreateGroupResponseDto.builder().id(groupId.value()).build();
  }
}
