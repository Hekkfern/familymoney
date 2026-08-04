package com.familymoney.application;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;

import com.familymoney.application.utils.FlowUtils;
import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  @MockitoBean private IEmailSenderService emailSenderService;

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
  void AuthFlow_register_login_refresh_and_logout_successfully() {
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
  void AuthFlow_Register_with_existing_email_fails() {
    // register and verify a new user
    final String username = FakeGenerator.username();
    final String email = FakeGenerator.email();
    final String password = FakeGenerator.password();
    flowUtils.registerAndVerifyNewUser(username, email, password);
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
  void AuthFlow_Login_with_unverified_email_fails() {
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
}
