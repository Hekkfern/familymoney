package com.familymoney.domains.idempotency.exceptions;

import lombok.experimental.StandardException;

@StandardException
public final class IdempotencyConflictException extends RuntimeException {}
