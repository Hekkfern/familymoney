package com.familymoney.familymoney.controllers.dtos.group;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateGroupResponseDto(UUID id) {}
