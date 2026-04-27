package com.familymoney.controllers.dtos.grouptransaction;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateGroupResponseDto(UUID id) {}
