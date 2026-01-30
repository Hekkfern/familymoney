package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GetGroupResponseDto(
    UUID id, String name, String description, String currency, Instant createdAt) {}
