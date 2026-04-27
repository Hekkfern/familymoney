package com.familymoney.controllers.dtos.user;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GetUserResponseDto(
    UUID id, String username, String email, Instant createdAt, boolean isEnabled) {}
