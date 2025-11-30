package com.familymoney.familymoney.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.familymoney.familymoney.dtos.auth.RegisterRequestDto;
import com.familymoney.familymoney.services.IEmailSenderService;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.utils.FakeGenerator;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthFlowTest {
  @Autowired private TestRestTemplate client;

  @MockitoBean private IEmailSenderService emailSenderService;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer("postgres:17.6-alpine");

  private final String BASE_AUTH_URI = "/api/auth";

  @Test
  void Auth_SuccessfulRegistrationProcess() {
    // Mock email sender
    val verificationTokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);

    // register the new user
    val registerResponse =
        client.postForEntity(
            String.format("%s/register", BASE_AUTH_URI),
            new RegisterRequestDto(
                FakeGenerator.username(), FakeGenerator.email(), FakeGenerator.password()),
            Void.class);
    assertNotNull(registerResponse);
    assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
    assertNull(registerResponse.getBody());

    // get the captured email verification token
    verify(emailSenderService)
        .sendEmailVerificationEmail(any(), any(), verificationTokenCaptor.capture());
    val verificationToken = verificationTokenCaptor.getValue();

    // verify email
    val verifyEmailResponse =
        client.getForEntity(
            String.format("%s/verify-email/%s", BASE_AUTH_URI, verificationToken), Void.class);
    assertNotNull(verifyEmailResponse);
    assertEquals(HttpStatus.OK, verifyEmailResponse.getStatusCode());
    assertNull(verifyEmailResponse.getBody());
  }
}
