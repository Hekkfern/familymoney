package com.familymoney.domains.idempotency.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;

public final class RequestCanonicalizer {

  private static final ObjectMapper canonicalMapper =
      JsonMapper.builder()
          .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
          .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
          .build();

  private RequestCanonicalizer() {
    /* This utility class should not be instantiated */
  }

  public static byte[] canonicalize(
      final HttpMethod method,
      final String path,
      final Map<String, List<String>> queryParameters,
      final @Nullable Object body) {
    final CanonicalRequest request =
        new CanonicalRequest(
            method.name(), path, canonicalizeQueryParameters(queryParameters), body);
    try {
      return canonicalMapper.writeValueAsBytes(request);
    } catch (final JsonProcessingException exception) {
      throw new RuntimeException("The request body cannot be canonicalized", exception);
    }
  }

  private static Map<String, List<String>> canonicalizeQueryParameters(
      final Map<String, List<String>> queryParameters) {
    final Map<String, List<String>> canonicalQueryParameters = new TreeMap<>();
    for (final Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
      final String parameterName = Objects.requireNonNull(entry.getKey());
      final List<String> parameterValues =
          Objects.requireNonNull(entry.getValue()).stream()
              .map(Objects::requireNonNull)
              .sorted(Comparator.naturalOrder())
              .toList();
      canonicalQueryParameters.put(parameterName, parameterValues);
    }
    return canonicalQueryParameters;
  }

  private record CanonicalRequest(
      String method,
      String path,
      Map<String, List<String>> queryParameters,
      @Nullable Object body) {}
}
