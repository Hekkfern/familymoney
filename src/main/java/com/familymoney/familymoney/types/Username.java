package com.familymoney.familymoney.types;

import org.springframework.lang.NonNull;

public record Username(String value) {

    @Override
    public @NonNull String toString() {
        return value;
    }
}
