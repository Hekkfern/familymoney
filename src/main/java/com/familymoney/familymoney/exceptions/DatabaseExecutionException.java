package com.familymoney.familymoney.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public final class DatabaseExecutionException extends ErrorResponseException {

  public DatabaseExecutionException(String message) {
    super(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "Please contact the administrator"),
        null);
  }
}
