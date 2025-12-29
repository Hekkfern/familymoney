package com.familymoney.familymoney.controllers.mappers;

import com.familymoney.familymoney.controllers.dtos.group.GetGroupResponseDto;
import com.familymoney.familymoney.services.data.GetGroupData;
import org.springframework.stereotype.Component;

@Component
public class GetGroupResponseMapper {

  public GetGroupResponseDto toDto(GetGroupData groupData) {
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
