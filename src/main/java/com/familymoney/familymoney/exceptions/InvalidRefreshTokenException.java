package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class InvalidRefreshTokenException extends RuntimeException {

  private final String message;
}
