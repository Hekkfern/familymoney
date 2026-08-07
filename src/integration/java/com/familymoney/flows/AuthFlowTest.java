package com.familymoney.flows;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;

import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LogoutRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshTokenRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.flows.utils.FlowUtils;
import com.familymoney.security.JwtUtils;
import com.familymoney.test_utils.DatabaseCrud;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import java.util.Locale;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class AuthFlowTest {

  // region Fields

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
}
