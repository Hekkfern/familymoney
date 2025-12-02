package com.familymoney.familymoney.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.familymoney.familymoney.dtos.auth.RegisterRequestDto;
import com.familymoney.familymoney.services.IEmailSenderService;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.utils.FakeGenerator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthFlowTests {

  // region Fields

  private RestTestClient client;

  @MockitoBean private IEmailSenderService emailSenderService;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer("postgres:17.6-alpine");

  private final String BASE_AUTH_URI = "/api/auth";

  @LocalServerPort private int port;

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void Auth_SuccessfulRegistrationProcess() {
    // Mock email sender
    val verificationTokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);

    // register the new user
    client
        .post()
        .uri(String.format("%s/register", BASE_AUTH_URI))
        .body(
            new RegisterRequestDto(
                FakeGenerator.username(), FakeGenerator.email(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();

    // get the captured email verification token
    verify(emailSenderService)
        .sendEmailVerificationEmail(any(), any(), verificationTokenCaptor.capture());
    val verificationToken = verificationTokenCaptor.getValue();

    // verify email
    client
        .get()
        .uri(String.format("%s/verify-email/%s", BASE_AUTH_URI, verificationToken))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
  }
}
