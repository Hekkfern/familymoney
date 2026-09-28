package com.familymoney.domains.idempotency.services;

import com.familymoney.domains.idempotency.exceptions.IdempotencyConflictException;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import com.fasterxml.jackson.databind.JavaType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Service that guarantees an action is executed at most once per idempotency key and user.
 *
 * <p>The first request for a key reserves it, executes the action, and stores the outcome. A
 * repeated request with the same key and an identical request replays the stored outcome without
 * executing the action again.
 */
public interface IdempotencyService {

  /**
   * Executes an action that produces a response body, or replays the stored response of a previous
   * identical request.
   *
   * @param idempotencyKey the key supplied by the client to identify the operation
   * @param userId the identifier of the user who performs the request
   * @param httpRequest the current HTTP request, used to fingerprint the method, path and query
   * @param requestBody the request body, used to fingerprint the request
   * @param responseType the type used to deserialize a stored response
   * @param action the action that produces the response
   * @param <T> the type of the response
   * @return the response produced by the action, or the stored response of a previous request
   * @throws IdempotencyConflictException if the key was used for a different request, or if the
   *     previous request is still in progress
   */
  <T> T runWithIdempotency(
      IdempotencyKey idempotencyKey,
      UserId userId,
      HttpServletRequest httpRequest,
      @Nullable Object requestBody,
      JavaType responseType,
      Supplier<T> action);

  /**
   * Executes an action without a response body, or skips it when a previous identical request has
   * already completed.
   *
   * @param idempotencyKey the key supplied by the client to identify the operation
   * @param userId the identifier of the user who performs the request
   * @param httpRequest the current HTTP request, used to fingerprint the method, path and query
   * @param requestBody the request body, used to fingerprint the request
   * @param action the action to execute
   * @throws IdempotencyConflictException if the key was used for a different request, or if the
   *     previous request is still in progress
   */
  void runWithIdempotency(
      IdempotencyKey idempotencyKey,
      UserId userId,
      HttpServletRequest httpRequest,
      @Nullable Object requestBody,
      Runnable action);
}
