package com.familymoney.familymoney.integration;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.familymoney.familymoney.controllers.dtos.auth.*;
import com.familymoney.familymoney.services.IEmailSenderService;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.utils.AuthControllerUriFactory;
import com.familymoney.familymoney.utils.FakeGenerator;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AuthFlowTests {

  // region Fields

  private RestTestClient client;

  @MockitoBean private IEmailSenderService emailSenderService;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @LocalServerPort private int port;

  // endregion

  // region Helpers

  private void registerAndVerifyNewUser(
      @NonNull String username, @NonNull String email, @NonNull String password) {
    // Mock email sender
    val verificationTokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);

    // register the new user
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username, email, password))
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
        .uri(AuthControllerUriFactory.getVerifyEmailPath(verificationToken.value()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
  }

  @NonNull
  private String[] loginUser(@NonNull String email, @NonNull String password) {
    val loginResponse =
        client
            .post()
            .uri(AuthControllerUriFactory.getLoginPath())
            .body(new LoginRequestDto(email, password))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(LoginResponseDto.class)
            .returnResult()
            .getResponseBody();
    return new String[] {loginResponse.accessToken(), loginResponse.refreshToken()};
  }

  @NonNull
  private String[] refreshTokens(@NonNull String refreshToken) {
    val refreshResponse =
        client
            .post()
            .uri(AuthControllerUriFactory.getRefreshPath())
            .body(new RefreshTokenRequestDto(refreshToken))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RefreshResponseDto.class)
            .returnResult()
            .getResponseBody();
    return new String[] {refreshResponse.accessToken(), refreshResponse.refreshToken()};
  }

  private void logoutUser(@NonNull String refreshToken) {
    client
        .post()
        .uri(AuthControllerUriFactory.getLogoutPath())
        .body(new LogoutRequestDto(refreshToken))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
  }

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void AuthFlow_register_login_refresh_and_logout_successfully() {
    // register and verify a new user
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    registerAndVerifyNewUser(username, email, password);

    // login
    val tokens = loginUser(email, password);
    val refreshToken = tokens[1];

    // refresh
    val newTokens = refreshTokens(refreshToken);
    val newRefreshToken = newTokens[1];

    // logout
    logoutUser(newRefreshToken);
  }

  @Test
  void Flow_Register_with_existing_email_fails() {
    // register and verify a new user
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    registerAndVerifyNewUser(username, email, password);
    // register again with the same email
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username, email, password))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void Flow_Login_with_unverified_email_fails() {
    // register a new user but do not verify email
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username, email, password))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
    // attempt to login
    val loginResponse =
        client
            .post()
            .uri(AuthControllerUriFactory.getLoginPath())
            .body(new LoginRequestDto(email, password))
            .exchange()
            .expectStatus()
            .isUnauthorized();
  }
}
