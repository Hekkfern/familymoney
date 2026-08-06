package com.familymoney.application;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.familymoney.application.utils.FlowUtils;
import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LogoutRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshTokenRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.auth.services.IAuthService;
import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthFlowTest {

  // region Fields

  private RestTestClient client;
  private FlowUtils flowUtils;

  @MockitoBean private IEmailSenderService emailSenderService;

  @Autowired private IAuthService authService;
  @Autowired private PlatformTransactionManager transactionManager;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @LocalServerPort private int port;

  // endregion

  @BeforeEach
  void setup() {
    this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    this.flowUtils = new FlowUtils(client, emailSenderService);
  }

  @Test
  void register_login_refresh_and_logout_successfully() {
    // register and verify a new user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);

    // login
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    final String refreshToken = tokens.refreshToken();

    // refresh
    final FlowUtils.TokenPair newTokens = flowUtils.refreshTokens(refreshToken);
    final String newRefreshToken = newTokens.refreshToken();

    // logout
    flowUtils.logoutUser(newRefreshToken);
  }

  @Test
  void registration_email_is_delivered_only_after_transaction_commit() {
    final UserName username = UserName.fromString(FakeGenerator.username());
    final Email email = Email.fromString(FakeGenerator.email());
    final Password password = Password.fromString(FakeGenerator.password());
    final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    transactionTemplate.executeWithoutResult(
        status -> {
          authService.registerUser(username, email, password);

          verify(emailSenderService, never())
              .sendEmailVerificationEmail(
                  any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
        });

    verify(emailSenderService, timeout(2000))
        .sendEmailVerificationEmail(eq(email), eq(username), any(EmailVerificationToken.class));
  }

  @Test
  void registration_email_is_not_delivered_when_transaction_rolls_back() {
    final UserName username = UserName.fromString(FakeGenerator.username());
    final Email email = Email.fromString(FakeGenerator.email());
    final Password password = Password.fromString(FakeGenerator.password());
    final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    transactionTemplate.executeWithoutResult(
        status -> {
          authService.registerUser(username, email, password);
          status.setRollbackOnly();
        });

    verify(emailSenderService, after(500).never())
        .sendEmailVerificationEmail(
            any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
  }

  @Test
  void register_with_existing_email_fails() {
    // register and verify a new user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    // register again with the same email
    final String username2 = FakeGenerator.username();
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username2, email, password))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void register_with_case_only_variant_email_fails() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);

    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(
            new RegisterRequestDto(
                FakeGenerator.username(), email.toUpperCase(Locale.ROOT), password))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void register_with_existing_username_fails() {
    // register and verify a new user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    // register again with the same username
    final String email2 = FakeGenerator.email();
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username, email2, password))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void login_with_unverified_email_fails() {
    // register a new user but do not verify email
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username, email, password))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
    // attempt to log in
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(email, password))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void login_with_different_case_email_succeeds() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    flowUtils.loginUser(email.toUpperCase(Locale.ROOT), password);
  }

  @Test
  void refresh_token_without_login_fails() {
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(FakeGenerator.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void login_without_registering_fails() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(FakeGenerator.email(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void logout_without_login_fails() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLogoutPath())
        .body(new LogoutRequestDto(FakeGenerator.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void refresh_token_after_logging_out_fails() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    flowUtils.logoutUser(tokens.refreshToken());

    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(tokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void reusing_refresh_token_invalidates_its_family() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    final FlowUtils.TokenPair refreshedTokens = flowUtils.refreshTokens(tokens.refreshToken());

    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(tokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(refreshedTokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
