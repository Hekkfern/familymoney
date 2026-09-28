package com.familymoney.domains.idempotency.services;

import com.familymoney.domains.idempotency.exceptions.IdempotencyConflictException;
import com.familymoney.domains.idempotency.repositories.IdempotencyRepository;
import com.familymoney.domains.idempotency.repositories.dtos.ReserveIdempotencyKeyDto;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyEntry;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyState;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.idempotency.utils.RequestCanonicalizer;
import com.familymoney.domains.users.types.UserId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultIdempotencyService implements IdempotencyService {

  private static final String DIFFERENT_REQUEST_MESSAGE =
      "The idempotency key was already used for a different request";
  private static final String IN_PROGRESS_MESSAGE = "The request is already in progress";
  private static final String MISSING_ENTRY_MESSAGE =
      "The idempotency entry was not found after a failed reservation";
  private static final String MISSING_BODY_MESSAGE =
      "The idempotency entry was completed without a response body";

  private final IdempotencyRepository idempotencyRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public <T> T runWithIdempotency(
      final IdempotencyKey idempotencyKey,
      final UserId userId,
      final HttpServletRequest httpRequest,
      final @Nullable Object requestBody,
      final JavaType responseType,
      final Supplier<T> action) {
    final byte[] requestHash = requestHash(httpRequest, requestBody);
    final IdempotencyEntry completedEntry =
        reserveOrReplay(idempotencyKey, userId, requestHash).orElse(null);
    if (completedEntry != null) {
      final String cachedBody = completedEntry.responseBody();
      if (cachedBody == null) {
        throw new IllegalStateException(MISSING_BODY_MESSAGE);
      }
      return deserializeResponse(cachedBody, responseType);
    }

    final T response = action.get();
    idempotencyRepository.complete(userId, idempotencyKey, serializeResponse(response));
    return response;
  }

  @Override
  public void runWithIdempotency(
      final IdempotencyKey idempotencyKey,
      final UserId userId,
      final HttpServletRequest httpRequest,
      final @Nullable Object requestBody,
      final Runnable action) {
    final byte[] requestHash = requestHash(httpRequest, requestBody);
    if (reserveOrReplay(idempotencyKey, userId, requestHash).isPresent()) {
      return;
    }

    action.run();
    idempotencyRepository.complete(userId, idempotencyKey, null);
  }

  private Optional<IdempotencyEntry> reserveOrReplay(
      final IdempotencyKey idempotencyKey, final UserId userId, final byte[] requestHash) {
    final IdempotencyEntry existingEntry =
        idempotencyRepository.findByKey(userId, idempotencyKey).orElse(null);
    if (existingEntry != null) {
      return Optional.of(handleExistingEntry(existingEntry, requestHash));
    }
    if (idempotencyRepository.reserve(
        new ReserveIdempotencyKeyDto(userId, idempotencyKey, requestHash))) {
      return Optional.empty();
    }

    final IdempotencyEntry reservedEntry =
        idempotencyRepository
            .findByKey(userId, idempotencyKey)
            .orElseThrow(() -> new IllegalStateException(MISSING_ENTRY_MESSAGE));
    return Optional.of(handleExistingEntry(reservedEntry, requestHash));
  }

  private byte[] requestHash(
      final HttpServletRequest httpRequest, final @Nullable Object requestBody) {
    final String path =
        httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
    final Map<String, List<String>> queryParameters =
        httpRequest.getParameterMap().entrySet().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    Map.Entry::getKey, entry -> List.of(entry.getValue())));
    return RequestCanonicalizer.canonicalize(
        HttpMethod.valueOf(httpRequest.getMethod()), path, queryParameters, requestBody);
  }

  private IdempotencyEntry handleExistingEntry(
      final IdempotencyEntry entry, final byte[] requestHash) {
    if (!Arrays.equals(entry.requestHash(), requestHash)) {
      throw new IdempotencyConflictException(DIFFERENT_REQUEST_MESSAGE);
    }
    if (entry.state() == IdempotencyState.IN_PROGRESS) {
      throw new IdempotencyConflictException(IN_PROGRESS_MESSAGE);
    }

    return entry;
  }

  private String serializeResponse(final Object response) {
    try {
      return objectMapper.writeValueAsString(response);
    } catch (final JsonProcessingException exception) {
      throw new IllegalStateException("The response body cannot be serialized", exception);
    }
  }

  private <T> T deserializeResponse(final String response, final JavaType responseType) {
    try {
      return objectMapper.readValue(response, responseType);
    } catch (final JsonProcessingException exception) {
      throw new IllegalStateException("The cached response cannot be read", exception);
    }
  }
}
