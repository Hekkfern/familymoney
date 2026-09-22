package com.familymoney.domains.transactions.repositories.exceptions;

import lombok.experimental.StandardException;

/** Indicates that a payment could not be updated. */
@StandardException
public final class UpdateBalanceException extends RuntimeException {}
