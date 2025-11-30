package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VerificationTokenExpiredException extends RuntimeException {

    private final String message;
}
