package com.familymoney.services.data;

import com.familymoney.types.Email;
import com.familymoney.types.UserId;
import com.familymoney.types.UserName;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UserData(
    UserId id,
    UserName username,
    Email email,
    Instant createdAt,
    boolean isEmailVerified,
    boolean isEnabled) {}
