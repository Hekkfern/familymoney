package com.familymoney.flows;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.familymoney.domains.auth.controllers.dtos.ForgotPasswordRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LogoutRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshTokenRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.auth.controllers.dtos.ResetPasswordRequestDto;
import com.familymoney.domains.auth.controllers.dtos.VerifyEmailRequestDto;
import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.flows.utils.FlowUtils;
import com.familymoney.security.DefaultOpaqueTokenHasher;
import com.familymoney.security.IOpaqueTokenHasher;
import com.familymoney.security.JwtUtils;
import com.familymoney.test_utils.DatabaseCrud;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthFlowTest {

  // region Fields

  private static final IOpaqueTokenHasher TOKEN_HASHER = new DefaultOpaqueTokenHasher();

  private RestTestClient client;
  private FlowUtils flowUtils;
  private DatabaseCrud databaseCrud;

  @MockitoBean private IEmailSenderService emailSenderService;
  @Autowired private DSLContext dslContext;
  @Autowired private JwtUtils jwtUtils;

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @LocalServerPort private int port;

  // endregion

  // region Helpers

  private EmailVerificationToken registerUnverifiedUserAndCaptureVerificationToken(
      final String username, final String email, final String password) {
    final ArgumentCaptor<EmailVerificationToken> verificationTokenCaptor =
        ArgumentCaptor.forClass(EmailVerificationToken.class);
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(new RegisterRequestDto(username, email, password))
        .exchange()
        .expectStatus()
        .isOk();
    verify(emailSenderService, timeout(2000))
        .sendEmailVerificationEmail(any(), any(), verificationTokenCaptor.capture());
    return verificationTokenCaptor.getValue();
  }

  private PasswordResetToken requestPasswordResetAndCaptureToken(final String email) {
    final ArgumentCaptor<PasswordResetToken> resetTokenCaptor =
        ArgumentCaptor.forClass(PasswordResetToken.class);
    client
        .post()
        .uri(AuthControllerUriFactory.getForgotPasswordPath())
        .body(new ForgotPasswordRequestDto(email))
        .exchange()
        .expectStatus()
        .isOk();
    verify(emailSenderService, timeout(2000))
        .sendPasswordResetEmail(any(), any(), resetTokenCaptor.capture());
    return resetTokenCaptor.getValue();
  }

  // endregion

  @BeforeEach
  void setup() {
    this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    this.flowUtils = new FlowUtils(client, emailSenderService);
    this.databaseCrud = new DatabaseCrud(dslContext);
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
  void register_with_existing_email_returns_conflict() {
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
  void register_with_case_only_variant_email_returns_conflict() {
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
  void register_with_existing_username_returns_conflict() {
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
  void login_with_unverified_email_returns_unauthorized() {
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
  void disabled_user_trying_to_login_or_refresh_tokens_returns_unauthorized() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    final UserId userId =
        jwtUtils
            .parseAccessToken(AccessToken.fromString(tokens.accessToken()))
            .orElseThrow()
            .userId();
    databaseCrud.enableUser(userId, false);

    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(email, password))
        .exchange()
        .expectStatus()
        .isUnauthorized();

    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(tokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void refresh_token_without_login_returns_unauthorized() {
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(FakeGenerator.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void login_without_registering_returns_unauthorized() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(FakeGenerator.email(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void logout_without_login_returns_unauthorized() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLogoutPath())
        .body(new LogoutRequestDto(FakeGenerator.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void refresh_token_after_logging_out_returns_unauthorized() {
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
  void expired_refresh_token_returns_unauthorized() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    databaseCrud.expireRefreshToken(RefreshToken.fromString(tokens.refreshToken()));

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

  @Test
  void login_with_wrong_password_returns_unauthorized() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);

    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(email, FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void verifying_an_unknown_email_token_returns_not_found() {
    client
        .post()
        .uri(AuthControllerUriFactory.getVerifyEmailPath())
        .body(new VerifyEmailRequestDto(FakeGenerator.emailVerificationToken()))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void verifying_an_expired_email_token_returns_gone() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final EmailVerificationToken token =
        registerUnverifiedUserAndCaptureVerificationToken(username, email, password);
    databaseCrud.expireEmailVerificationToken(token);

    client
        .post()
        .uri(AuthControllerUriFactory.getVerifyEmailPath())
        .body(new VerifyEmailRequestDto(token.value()))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.GONE);
  }

  @Test
  void verifying_an_email_token_twice_returns_not_found_on_second_attempt() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final EmailVerificationToken token =
        registerUnverifiedUserAndCaptureVerificationToken(username, email, password);

    client
        .post()
        .uri(AuthControllerUriFactory.getVerifyEmailPath())
        .body(new VerifyEmailRequestDto(token.value()))
        .exchange()
        .expectStatus()
        .isOk();

    client
        .post()
        .uri(AuthControllerUriFactory.getVerifyEmailPath())
        .body(new VerifyEmailRequestDto(token.value()))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void resending_verification_email_replaces_the_existing_token() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final EmailVerificationToken originalToken =
        registerUnverifiedUserAndCaptureVerificationToken(username, email, password);
    databaseCrud.setEmailVerificationTokenLastSentAt(
        originalToken, OffsetDateTime.now().minusMinutes(3));

    final ArgumentCaptor<EmailVerificationToken> replacementTokenCaptor =
        ArgumentCaptor.forClass(EmailVerificationToken.class);
    client
        .post()
        .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
        .body(Map.of("email", email))
        .exchange()
        .expectStatus()
        .isAccepted();
    verify(emailSenderService, timeout(2000).times(2))
        .sendEmailVerificationEmail(any(), any(), replacementTokenCaptor.capture());
    final EmailVerificationToken replacementToken = replacementTokenCaptor.getValue();

    client
        .post()
        .uri(AuthControllerUriFactory.getVerifyEmailPath())
        .body(new VerifyEmailRequestDto(originalToken.value()))
        .exchange()
        .expectStatus()
        .isNotFound();

    client
        .post()
        .uri(AuthControllerUriFactory.getVerifyEmailPath())
        .body(new VerifyEmailRequestDto(replacementToken.value()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void resending_verification_email_too_soon_returns_rate_limit_headers() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    registerUnverifiedUserAndCaptureVerificationToken(username, email, password);

    client
        .post()
        .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
        .body(Map.of("email", email))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
        .expectHeader()
        .value(HttpHeaders.RETRY_AFTER, value -> assertTrue(Long.parseLong(value) > 0))
        .expectHeader()
        .valueEquals("RateLimit-Limit", "1")
        .expectHeader()
        .valueEquals("RateLimit-Remaining", "0")
        .expectHeader()
        .exists("RateLimit-Reset");
  }

  @Test
  void resending_verification_email_for_unknown_account_does_not_send_an_email() {
    client
        .post()
        .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
        .body(Map.of("email", FakeGenerator.email()))
        .exchange()
        .expectStatus()
        .isAccepted();

    verifyNoInteractions(emailSenderService);
  }

  @Test
  void resending_verification_email_for_verified_account_does_not_send_an_email() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    clearInvocations(emailSenderService);

    client
        .post()
        .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
        .body(Map.of("email", email))
        .exchange()
        .expectStatus()
        .isAccepted();

    verifyNoInteractions(emailSenderService);
  }

  @Test
  void forgot_password_then_reset_updates_credentials_and_revokes_existing_refresh_tokens() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    final String newPassword = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    clearInvocations(emailSenderService);

    final PasswordResetToken resetToken = requestPasswordResetAndCaptureToken(email);
    client
        .post()
        .uri(AuthControllerUriFactory.getResetPasswordPath())
        .body(new ResetPasswordRequestDto(resetToken.value(), newPassword))
        .exchange()
        .expectStatus()
        .isOk();

    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(email, password))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    flowUtils.loginUser(email, newPassword);
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(tokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
    client
        .post()
        .uri(AuthControllerUriFactory.getResetPasswordPath())
        .body(new ResetPasswordRequestDto(resetToken.value(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void forgot_password_for_unknown_account_returns_success_without_sending_email() {
    client
        .post()
        .uri(AuthControllerUriFactory.getForgotPasswordPath())
        .body(new ForgotPasswordRequestDto(FakeGenerator.email()))
        .exchange()
        .expectStatus()
        .isOk();

    verifyNoInteractions(emailSenderService);
  }

  @Test
  void forgot_password_for_disabled_account_returns_success_without_sending_email() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    final UserId userId =
        jwtUtils
            .parseAccessToken(AccessToken.fromString(tokens.accessToken()))
            .orElseThrow()
            .userId();
    databaseCrud.enableUser(userId, false);
    clearInvocations(emailSenderService);

    client
        .post()
        .uri(AuthControllerUriFactory.getForgotPasswordPath())
        .body(new ForgotPasswordRequestDto(email))
        .exchange()
        .expectStatus()
        .isOk();

    verifyNoInteractions(emailSenderService);
  }

  @Test
  void requesting_password_reset_too_soon_returns_success_without_sending_another_email() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    clearInvocations(emailSenderService);

    requestPasswordResetAndCaptureToken(email);
    client
        .post()
        .uri(AuthControllerUriFactory.getForgotPasswordPath())
        .body(new ForgotPasswordRequestDto(email))
        .exchange()
        .expectStatus()
        .isOk();

    verify(emailSenderService, timeout(2000).times(1)).sendPasswordResetEmail(any(), any(), any());
  }

  @Test
  void resetting_password_with_unknown_token_returns_not_found() {
    client
        .post()
        .uri(AuthControllerUriFactory.getResetPasswordPath())
        .body(
            new ResetPasswordRequestDto(
                PasswordResetToken.generate().value(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void resetting_password_with_expired_token_returns_gone() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    clearInvocations(emailSenderService);
    final PasswordResetToken resetToken = requestPasswordResetAndCaptureToken(email);
    databaseCrud.expirePasswordResetToken(resetToken);

    client
        .post()
        .uri(AuthControllerUriFactory.getResetPasswordPath())
        .body(new ResetPasswordRequestDto(resetToken.value(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.GONE);
  }

  @Test
  void logging_out_with_an_expired_refresh_token_returns_unauthorized() {
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair tokens = flowUtils.loginUser(email, password);
    databaseCrud.expireRefreshToken(RefreshToken.fromString(tokens.refreshToken()));

    client
        .post()
        .uri(AuthControllerUriFactory.getLogoutPath())
        .body(new LogoutRequestDto(tokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void logging_out_with_a_blacklisted_token_family_returns_unauthorized() {
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
        .uri(AuthControllerUriFactory.getLogoutPath())
        .body(new LogoutRequestDto(refreshedTokens.refreshToken()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
