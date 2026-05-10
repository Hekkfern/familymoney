package com.familymoney.domains.transactions.controllers.dtos;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GetUsersInGroupResponseDto(List<UUID> userIds) {}
