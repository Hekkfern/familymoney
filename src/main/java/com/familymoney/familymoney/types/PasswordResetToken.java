package com.familymoney.familymoney.types;

import org.springframework.lang.NonNull;

public record PasswordResetToken(String value) {

    @Override
    public @NonNull String toString() {
        return value;
    }
}
