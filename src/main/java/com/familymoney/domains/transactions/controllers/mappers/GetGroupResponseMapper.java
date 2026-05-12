package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.services.data.GroupData;
import org.springframework.stereotype.Component;

@Component
public class GetGroupResponseMapper {

  public GetGroupResponseDto toDto(GroupData groupData) {
    return GetGroupResponseDto.builder()
        .id(groupData.id().value())
        .name(groupData.name().value())
        .description(groupData.description())
        .currency(groupData.currency().getCurrencyCode())
        .createdAt(groupData.createdAt())
        .build();
  }
}
