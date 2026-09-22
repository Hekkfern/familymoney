package com.familymoney.domains.transactions.repositories.exceptions;

import lombok.experimental.StandardException;

/** Indicates that a payment could not be deleted. */
@StandardException
public final class DeletePaymentException extends RuntimeException {}
