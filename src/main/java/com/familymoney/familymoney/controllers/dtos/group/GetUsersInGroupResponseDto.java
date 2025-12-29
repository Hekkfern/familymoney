package com.familymoney.familymoney.controllers.dtos.group;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GetUsersInGroupResponseDto(List<UUID> userIds) {}
