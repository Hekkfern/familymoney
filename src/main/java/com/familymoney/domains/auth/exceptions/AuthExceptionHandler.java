package com.familymoney.domains.auth.exceptions;

import com.familymoney.domains.auth.controllers.AuthController;
import com.familymoney.domains.auth.services.AuthService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(assignableTypes = {AuthController.class, AuthService.class})
@RequiredArgsConstructor
public class AuthExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String RATE_LIMIT_LIMIT_HEADER = "RateLimit-Limit";
  private static final String RATE_LIMIT_REMAINING_HEADER = "RateLimit-Remaining";
  private static final String RATE_LIMIT_RESET_HEADER = "RateLimit-Reset";

  private final Clock clock;

  @ExceptionHandler(NoSuchElementException.class)
  public ProblemDetail handle(NoSuchElementException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

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

  @ExceptionHandler(RefreshTokenNotFoundException.class)
  public ProblemDetail handle(RefreshTokenNotFoundException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler({RefreshTokenReuseDetectedException.class, BlacklistedFamilyException.class})
  public ProblemDetail handle(final RuntimeException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
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

  @ExceptionHandler(UserNotEnabledException.class)
  public ProblemDetail handle(UserNotEnabledException e) {
    logger.info(e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(NewEmailVerificationTooSoonException.class)
  public ResponseEntity<ProblemDetail> handle(final NewEmailVerificationTooSoonException e) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(toRetryAfterSeconds(e.getNextRequestAt())));
    headers.set(RATE_LIMIT_LIMIT_HEADER, "1");
    headers.set(RATE_LIMIT_REMAINING_HEADER, "0");
    headers.set(RATE_LIMIT_RESET_HEADER, String.valueOf(e.getNextRequestAt().getEpochSecond()));
    final ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS, "Too many verification email requests");
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(headers).body(problem);
  }

  private long toRetryAfterSeconds(final Instant nextRequestAt) {
    final Duration retryAfter = Duration.between(Instant.now(clock), nextRequestAt);
    return Math.max(1, retryAfter.getSeconds());
  }
}
