package com.familymoney.domains.auth.exceptions;

import com.familymoney.domains.auth.controllers.AuthController;
import com.familymoney.domains.auth.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(assignableTypes = {AuthController.class, AuthService.class})
public class AuthExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handle(BadCredentialsException e) {
    logger.info(e.getMessage());
    // SECURITY: Never return specific reason for authentication failure
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ProblemDetail handle(UserAlreadyExistsException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(EmailNotFoundException.class)
  public ProblemDetail handle(EmailNotFoundException e) {
    logger.info(e.getMessage());
    // SECURITY: Always return success even if email doesn't exist
    // This prevents attackers from discovering which emails are registered
    return ProblemDetail.forStatusAndDetail(HttpStatus.OK, "");
  }

  @ExceptionHandler(RefreshTokenNotFoundException.class)
  public ProblemDetail handle(RefreshTokenNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(VerificationTokenNotFoundException.class)
  public ProblemDetail handle(VerificationTokenNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(VerificationTokenExpiredException.class)
  public ProblemDetail handle(VerificationTokenExpiredException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, e.getMessage());
  }

  @ExceptionHandler(ResetPasswordTokenNotFoundException.class)
  public ProblemDetail handle(ResetPasswordTokenNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ResetPasswordTokenExpiredException.class)
  public ProblemDetail handle(ResetPasswordTokenExpiredException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, e.getMessage());
  }
}
