package com.familymoney.familymoney.types;

import org.springframework.lang.NonNull;
import java.util.UUID;

public record UserId(UUID value) {

    @Override
    public @NonNull String toString() {
        return value.toString();
    }

    public static UserId fromString(String value) {
        return new UserId(UUID.fromString(value));
    }
}
