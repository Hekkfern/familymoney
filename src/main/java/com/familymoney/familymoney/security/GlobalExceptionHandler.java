package com.familymoney.familymoney.security;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(DatabaseExecutionException.class)
  public ResponseEntity<@NonNull String> handle(DatabaseExecutionException e) {
    logger.error(e.getMessage());
    // SECURITY: Always return success even if email doesn't exist
    // This prevents attackers from discovering which emails are registered
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
  }

  /**
   * Errors that the developer did not expect are handled here and the log level is recorded as
   * error.
   *
   * @param e Exception
   * @return ResponseEntity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<@NonNull String> handle(Exception e) {
    logger.error(e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Please contact the administrator.");
  }
}
