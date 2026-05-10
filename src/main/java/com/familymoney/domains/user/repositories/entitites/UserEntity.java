package com.familymoney.domains.user.repositories.entitites;

import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
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
