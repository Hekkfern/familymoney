package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import java.time.Instant;
import lombok.Builder;

@Builder
public record GetUserData(
    UserId id,
    UserName username,
    Email email,
    Instant createdAt,
    boolean isEmailVerified,
    boolean isEnabled) {}
