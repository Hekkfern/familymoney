package com.familymoney.domains.users.controllers.dtos;

import java.time.Instant;
import java.util.UUID;

public record GetUserResponseDto(
    UUID id,
    String username,
    String email,
    Instant createdAt,
    boolean isEmailVerified,
    boolean isEnabled) {}
