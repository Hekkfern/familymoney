package com.familymoney.familymoney.types;

import java.util.UUID;
import org.springframework.lang.NonNull;

public record UserId(UUID value) {

    @Override
    public @NonNull String toString() {
        return value.toString();
    }

    public static UserId fromString(String value) {
        return new UserId(UUID.fromString(value));
    }
}
