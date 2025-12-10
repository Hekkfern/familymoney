package com.familymoney.familymoney.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(DatabaseExecutionException.class)
  public ProblemDetail handleDatabaseExecutionException(DatabaseExecutionException e) {
    logger.error(e.getMessage());
    // Don't return any message to avoid leaking sensitive information
    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "");
  }
}
