package com.familymoney.repositories.entities;

import com.familymoney.types.Email;
import com.familymoney.types.UserId;
import com.familymoney.types.UserName;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UserEntity(
    UserId id,
    UserName username,
    Email email,
    String hashedPassword,
    Instant createdAt,
    Instant updatedAt,
    boolean isEmailVerified,
    boolean isEnabled) {}
