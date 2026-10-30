package com.familymoney.domains.users.repositories.entitites;

import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;

public record UserEntity(
    UserId id,
    UserName username,
    Email email,
    String hashedPassword,
    boolean isEmailVerified,
    boolean isEnabled) {}
