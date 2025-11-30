package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import lombok.Builder;
import org.springframework.lang.NonNull;
import java.time.Instant;

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
