package com.familymoney.domains.transactions.controllers.dtos;

import java.util.List;
import lombok.Builder;

@Builder
public record GetGroupsResponseDto(List<GetGroupResponseDto> groups) {}
