package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.types.GroupId;
import lombok.Builder;

@Builder
public record CreateGroupResponseDto(GroupId id) {}
