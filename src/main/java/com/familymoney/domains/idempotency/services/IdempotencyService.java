package com.familymoney.domains.idempotency.services;

import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import com.fasterxml.jackson.databind.JavaType;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public interface IdempotencyService {

  <T> T runWithIdempotency(
      IdempotencyKey idempotencyKey,
      UserId userId,
      HttpServletRequest httpRequest,
      @Nullable Object requestBody,
      JavaType responseType,
      HttpStatus successStatus,
      Supplier<T> action);
}
