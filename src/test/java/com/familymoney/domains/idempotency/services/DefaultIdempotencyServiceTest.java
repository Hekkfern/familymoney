package com.familymoney.domains.idempotency.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.domains.idempotency.exceptions.IdempotencyConflictException;
import com.familymoney.domains.idempotency.repositories.IdempotencyRepository;
import com.familymoney.domains.idempotency.repositories.dtos.CachedResponseDto;
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
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DefaultIdempotencyServiceTest {

  private static final String PATH = "/groups";
  private static final Instant EXPIRATION = Instant.parse("2025-01-01T00:01:00Z");

  @Mock private IdempotencyRepository idempotencyRepository;
  @Mock private HttpServletRequest httpRequest;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final UserId userId = UserId.fromUuid(UUID.randomUUID());
  private final IdempotencyKey idempotencyKey = IdempotencyKey.fromUuid(UUID.randomUUID());
  private final TestRequest request = new TestRequest("name");
  private final JavaType responseType = objectMapper.constructType(TestResponse.class);

  private DefaultIdempotencyService idempotencyService;

  @BeforeEach
  void setUp() {
    idempotencyService = new DefaultIdempotencyService(idempotencyRepository, objectMapper);
    when(httpRequest.getMethod()).thenReturn(HttpMethod.POST.name());
    when(httpRequest.getRequestURI()).thenReturn(PATH);
    when(httpRequest.getContextPath()).thenReturn("");
    when(httpRequest.getParameterMap()).thenReturn(Map.of());
  }

  private byte[] requestHash() {
    return RequestCanonicalizer.canonicalize(HttpMethod.POST, PATH, Map.of(), request);
  }

  private IdempotencyEntry entry(
      final byte[] requestHash, final IdempotencyState state, final CachedResponseDto response) {
    return new IdempotencyEntry(userId, idempotencyKey, requestHash, state, response, EXPIRATION);
  }

  private TestResponse run(final Supplier<TestResponse> action) {
    return idempotencyService.runWithIdempotency(
        idempotencyKey, userId, httpRequest, request, responseType, HttpStatus.CREATED, action);
  }

  @Nested
  class RunWithIdempotency {

    @Test
    void executes_action_and_caches_response_when_key_is_reserved() {
      final TestResponse expected = new TestResponse("id");
      when(idempotencyRepository.findByKey(userId, idempotencyKey)).thenReturn(Optional.empty());
      when(idempotencyRepository.reserve(any(ReserveIdempotencyKeyDto.class))).thenReturn(true);

      final TestResponse result = run(() -> expected);

      assertThat(result).isEqualTo(expected);
      final ArgumentCaptor<CachedResponseDto> responseCaptor =
          ArgumentCaptor.forClass(CachedResponseDto.class);
      verify(idempotencyRepository).complete(userId, idempotencyKey, responseCaptor.capture());
      assertThat(responseCaptor.getValue())
          .isEqualTo(new CachedResponseDto(HttpStatus.CREATED.value(), "{\"id\":\"id\"}"));
    }

    @Test
    void returns_cached_response_when_matching_request_is_completed() {
      final CachedResponseDto cachedResponse =
          new CachedResponseDto(HttpStatus.CREATED.value(), "{\"id\":\"cached\"}");
      when(idempotencyRepository.findByKey(userId, idempotencyKey))
          .thenReturn(
              Optional.of(entry(requestHash(), IdempotencyState.COMPLETED, cachedResponse)));

      final TestResponse result = run(() -> new TestResponse("new"));

      assertThat(result).isEqualTo(new TestResponse("cached"));
      verify(idempotencyRepository, never()).reserve(any());
      verify(idempotencyRepository, never()).complete(any(), any(), any());
    }

    @Test
    void rejects_reuse_for_a_different_request() {
      when(idempotencyRepository.findByKey(userId, idempotencyKey))
          .thenReturn(Optional.of(entry(new byte[] {1}, IdempotencyState.IN_PROGRESS, null)));

      assertThatThrownBy(() -> run(() -> new TestResponse("new")))
          .isInstanceOf(IdempotencyConflictException.class)
          .hasMessage("The idempotency key was already used for a different request");

      verify(idempotencyRepository, never()).reserve(any());
    }

    @Test
    void rejects_matching_request_while_it_is_in_progress() {
      when(idempotencyRepository.findByKey(userId, idempotencyKey))
          .thenReturn(Optional.of(entry(requestHash(), IdempotencyState.IN_PROGRESS, null)));

      assertThatThrownBy(() -> run(() -> new TestResponse("new")))
          .isInstanceOf(IdempotencyConflictException.class)
          .hasMessage("The request is already in progress");

      verify(idempotencyRepository, never()).reserve(any());
    }

    @Test
    void returns_cached_response_when_reservation_is_lost() {
      final CachedResponseDto cachedResponse =
          new CachedResponseDto(HttpStatus.CREATED.value(), "{\"id\":\"cached\"}");
      when(idempotencyRepository.findByKey(userId, idempotencyKey))
          .thenReturn(Optional.empty())
          .thenReturn(
              Optional.of(entry(requestHash(), IdempotencyState.COMPLETED, cachedResponse)));
      when(idempotencyRepository.reserve(any(ReserveIdempotencyKeyDto.class))).thenReturn(false);

      final TestResponse result = run(() -> new TestResponse("new"));

      assertThat(result).isEqualTo(new TestResponse("cached"));
      verify(idempotencyRepository, never()).complete(any(), any(), any());
    }

    @Test
    void does_not_complete_key_when_action_fails() {
      when(idempotencyRepository.findByKey(userId, idempotencyKey)).thenReturn(Optional.empty());
      when(idempotencyRepository.reserve(any(ReserveIdempotencyKeyDto.class))).thenReturn(true);

      assertThatThrownBy(
              () ->
                  run(
                      () -> {
                        throw new IllegalStateException("failed");
                      }))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("failed");

      verify(idempotencyRepository, never()).complete(any(), any(), any());
    }

    @Test
    void does_not_complete_key_when_response_cannot_be_serialized() throws JsonProcessingException {
      final ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
      final DefaultIdempotencyService failingIdempotencyService =
          new DefaultIdempotencyService(idempotencyRepository, failingObjectMapper);
      when(idempotencyRepository.findByKey(userId, idempotencyKey)).thenReturn(Optional.empty());
      when(idempotencyRepository.reserve(any(ReserveIdempotencyKeyDto.class))).thenReturn(true);
      when(failingObjectMapper.writeValueAsString(any()))
          .thenThrow(new JsonProcessingException("cannot serialize") {});

      assertThatThrownBy(
              () ->
                  failingIdempotencyService.runWithIdempotency(
                      idempotencyKey,
                      userId,
                      httpRequest,
                      request,
                      responseType,
                      HttpStatus.CREATED,
                      () -> new TestResponse("id")))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("The response body cannot be serialized");

      verify(idempotencyRepository, never()).complete(any(), any(), any());
    }
  }

  private record TestRequest(String name) {}

  private record TestResponse(String id) {}
}
