package com.familymoney.domains.auth.controllers.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LoginRequestDtoTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  void equality_and_hashcode() {
    final var a = new LoginRequestDto("bob@example.com", "secret");
    final var b = new LoginRequestDto("bob@example.com", "secret");
    final var c = new LoginRequestDto("bob@example.com", "different");
    final var d = new LoginRequestDto("alice@example.com", "secret");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
    assertNotEquals(a, d);
  }

  @Test
  void constructors_and_accessors() {
    final var email = "carol@example.com";
    final var password = "hunter2";
    final var dto = new LoginRequestDto(email, password);

    assertEquals(email, dto.email());
    assertEquals(password, dto.password());
  }

  @Test
  void jackson_serialization_and_deserialization() throws Exception {
    final var dto = new LoginRequestDto("alice@example.com", "P@ssw0rd");

    final var json = mapper.writeValueAsString(dto);
    final var expectedJson = "{\"email\":\"alice@example.com\",\"password\":\"P@ssw0rd\"}";
    assertEquals(expectedJson, json);
    final var read = mapper.readValue(json, LoginRequestDto.class);

    assertEquals(dto, read);
  }
}
