package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RefreshTokenNotFoundException extends RuntimeException {

    private final String message;
}
