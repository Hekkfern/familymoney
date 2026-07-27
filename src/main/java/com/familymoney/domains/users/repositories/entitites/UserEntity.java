package com.familymoney.domains.users.repositories.entitites;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import java.time.Instant;

public record UserEntity(
    UserId id,
    UserName username,
    Email email,
    String hashedPassword,
    Instant createdAt,
    Instant updatedAt,
    boolean isEmailVerified,
    boolean isEnabled) {}
