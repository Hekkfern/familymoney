package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VerificationTokenNotFoundException extends RuntimeException {

  private final String message;
}
