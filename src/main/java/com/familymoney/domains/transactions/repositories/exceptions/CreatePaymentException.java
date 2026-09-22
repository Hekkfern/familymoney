package com.familymoney.domains.transactions.repositories.exceptions;

import lombok.experimental.StandardException;

/** Indicates that a payment could not be created. */
@StandardException
public final class CreatePaymentException extends RuntimeException {}
