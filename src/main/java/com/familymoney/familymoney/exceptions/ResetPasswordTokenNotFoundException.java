package com.familymoney.familymoney.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ResetPasswordTokenNotFoundException extends RuntimeException {

  private final String message;
}
