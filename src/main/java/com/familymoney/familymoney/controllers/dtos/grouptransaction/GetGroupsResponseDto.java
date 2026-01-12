package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import java.util.List;
import lombok.Builder;

@Builder
public record GetGroupsResponseDto(List<GetGroupResponseDto> groups) {}
