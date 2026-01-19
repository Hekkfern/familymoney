package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.GetGroupResponseDto;
import com.familymoney.familymoney.services.data.GroupData;
import org.springframework.stereotype.Component;

@Component
public class GetGroupResponseMapper {

  public GetGroupResponseDto toDto(GroupData groupData) {
    return GetGroupResponseDto.builder()
        .id(groupData.id().value())
        .name(groupData.name().value())
        .description(groupData.description())
        .currency(groupData.currency().getCurrencyCode())
        .createdBy(groupData.createdBy().value())
        .createdAt(groupData.createdAt())
        .build();
  }
}
