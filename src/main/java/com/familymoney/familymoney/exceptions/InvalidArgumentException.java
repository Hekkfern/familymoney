package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class InvalidArgumentException extends RuntimeException {

  private final String message;
}
