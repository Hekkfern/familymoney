package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import org.springframework.lang.NonNull;
import java.time.Instant;
import java.util.UUID;

public record PasswordResetDbo(
        @NonNull UUID id,
        @NonNull UserId userId,
        @NonNull PasswordResetToken token,
        @NonNull Instant createdAt,
        @NonNull Instant expiresAt) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
