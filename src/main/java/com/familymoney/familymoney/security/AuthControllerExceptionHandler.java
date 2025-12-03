package com.familymoney.familymoney.security;

import com.familymoney.familymoney.controllers.AuthController;
import com.familymoney.familymoney.exceptions.EmailNotFoundException;
import com.familymoney.familymoney.exceptions.RefreshTokenNotFoundException;
import com.familymoney.familymoney.exceptions.ResetPasswordTokenExpiredException;
import com.familymoney.familymoney.exceptions.ResetPasswordTokenNotFoundException;
import com.familymoney.familymoney.exceptions.UserAlreadyExistsException;
import com.familymoney.familymoney.exceptions.VerificationTokenExpiredException;
import com.familymoney.familymoney.exceptions.VerificationTokenNotFoundException;
import com.familymoney.familymoney.services.AuthService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(assignableTypes = {AuthController.class, AuthService.class})
public class AuthControllerExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<@NonNull String> handle(BadCredentialsException e) {
    logger.info(e.getMessage());
    // SECURITY: Never return specific reason for authentication failure
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<@NonNull String> handle(UserAlreadyExistsException e) {
    logger.info(e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
  }

  @ExceptionHandler(EmailNotFoundException.class)
  public ResponseEntity<@NonNull String> handle(EmailNotFoundException e) {
    logger.info(e.getMessage());
    // SECURITY: Always return success even if email doesn't exist
    // This prevents attackers from discovering which emails are registered
    return ResponseEntity.status(HttpStatus.OK).body("");
  }

  @ExceptionHandler(RefreshTokenNotFoundException.class)
  public ResponseEntity<@NonNull String> handle(RefreshTokenNotFoundException e) {
    logger.info(e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
  }

  @ExceptionHandler(VerificationTokenNotFoundException.class)
  public ResponseEntity<@NonNull String> handle(VerificationTokenNotFoundException e) {
    logger.info(e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(VerificationTokenExpiredException.class)
  public ResponseEntity<@NonNull String> handle(VerificationTokenExpiredException e) {
    logger.info(e.getMessage());
    return ResponseEntity.status(HttpStatus.GONE).body(e.getMessage());
  }

  @ExceptionHandler(ResetPasswordTokenNotFoundException.class)
  public ResponseEntity<@NonNull String> handle(ResetPasswordTokenNotFoundException e) {
    logger.info(e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(ResetPasswordTokenExpiredException.class)
  public ResponseEntity<@NonNull String> handle(ResetPasswordTokenExpiredException e) {
    logger.info(e.getMessage());
    return ResponseEntity.status(HttpStatus.GONE).body(e.getMessage());
  }
}
