package com.familymoney.familymoney.controllers.dtos.admin;

import java.time.Instant;
import org.jspecify.annotations.NonNull;

public record GetUserResponseDto(
    @NonNull String username,
    @NonNull String email,
    @NonNull Instant createdAt,
    boolean isEnabled) {}
