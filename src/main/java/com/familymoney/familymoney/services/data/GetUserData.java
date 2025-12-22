package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserName;
import lombok.Builder;

import java.time.Instant;

@Builder
public record GetUserData(
    UserName username,
    Email email,
    Instant createdAt,
    boolean isEmailVerified,
    boolean isEnabled) {}
