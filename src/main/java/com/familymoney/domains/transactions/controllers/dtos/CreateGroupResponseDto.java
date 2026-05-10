package com.familymoney.domains.transactions.controllers.dtos;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateGroupResponseDto(UUID id) {}
