package com.familymoney.familymoney.repositories.entities;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
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
