package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.Username;
import java.time.Instant;
import org.jspecify.annotations.NonNull;

public record GetUserData(
    @NonNull Username username,
    @NonNull Email email,
    @NonNull Instant createdAt,
    boolean emailVerified,
    boolean isEnabled,
    Role role) {}
