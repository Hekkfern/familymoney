package com.familymoney.familymoney.dtos.user;

import java.time.Instant;
import org.jspecify.annotations.NonNull;

public record GetMyUserResponseDto(
    @NonNull String username, @NonNull String email, @NonNull Instant createdAt) {}
