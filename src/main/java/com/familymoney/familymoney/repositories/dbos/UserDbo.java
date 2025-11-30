package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import org.springframework.lang.NonNull;
import java.time.Instant;

public record UserDbo(
        @NonNull UserId id,
        @NonNull Username username,
        @NonNull Email email,
        @NonNull String hashedPassword,
        @NonNull Instant createdAt,
        @NonNull Instant updatedAt,
        boolean emailVerified,
        boolean isEnabled) {

}
