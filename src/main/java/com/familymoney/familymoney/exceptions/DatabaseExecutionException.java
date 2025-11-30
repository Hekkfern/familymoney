package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DatabaseExecutionException extends RuntimeException {

    private final String message;
}
