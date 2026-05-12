package com.familymoney.domains.user.controllers.dtos;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GetUserResponseDto(
    UUID id, String username, String email, Instant createdAt, boolean isEmailVerified, boolean isEnabled) {}
