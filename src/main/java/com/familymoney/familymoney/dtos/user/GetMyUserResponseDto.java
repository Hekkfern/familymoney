package com.familymoney.familymoney.dtos.user;

import org.springframework.lang.NonNull;
import java.time.Instant;

public record GetMyUserResponseDto(
        @NonNull String username,
        @NonNull String email,
        @NonNull Instant createdAt) {

}
