package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ResetPasswordTokenExpiredException extends RuntimeException {

    private final String message;
}
