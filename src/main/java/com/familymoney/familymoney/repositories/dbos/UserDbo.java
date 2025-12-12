package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UserDbo(
    UserId id,
    Username username,
    Email email,
    String hashedPassword,
    Instant createdAt,
    Instant updatedAt,
    boolean emailVerified,
    boolean isEnabled) {}
