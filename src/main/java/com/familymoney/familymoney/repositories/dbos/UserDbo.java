package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record UserDbo(
    @NonNull UserId id,
    @NonNull Username username,
    @NonNull Email email,
    @NonNull String hashedPassword,
    @NonNull Instant createdAt,
    @NonNull Instant updatedAt,
    boolean emailVerified,
    boolean isEnabled) {}
