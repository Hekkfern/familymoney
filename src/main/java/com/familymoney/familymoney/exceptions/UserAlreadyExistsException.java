package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class UserAlreadyExistsException extends RuntimeException {

  private final String message;
}
