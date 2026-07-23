package com.familymoney.domains.user.services.data;

import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import java.time.Instant;

public record UserData(
    UserId id,
    UserName username,
    Email email,
    Instant createdAt,
    boolean isEmailVerified,
    boolean isEnabled) {}
