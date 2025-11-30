package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EmailNotFoundException extends RuntimeException {

  private final String message;
}
