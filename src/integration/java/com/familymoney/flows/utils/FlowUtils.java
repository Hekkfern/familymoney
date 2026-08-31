package com.familymoney.flows.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LoginResponseDto;
import com.familymoney.domains.auth.controllers.dtos.LogoutRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshResponseDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshTokenRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.auth.controllers.dtos.VerifyEmailRequestDto;
import com.familymoney.domains.auth.services.EmailSenderService;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.testutils.AuthControllerUriFactory;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.client.RestTestClient;

@RequiredArgsConstructor
public class FlowUtils {

  private final RestTestClient client;
  private final EmailSenderService emailSenderService;

  public void registerAndVerifyNewUser(
      final String username, final String email, final String password) {
    // Mock email sender
    final ArgumentCaptor<EmailVerificationToken> verificationTokenCaptor =
        ArgumentCaptor.forClass(EmailVerificationToken.class);

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
    verify(emailSenderService, timeout(2000))
        .sendEmailVerificationEmail(any(), any(), verificationTokenCaptor.capture());
    final EmailVerificationToken verificationToken = verificationTokenCaptor.getValue();

    // verify email
    client
        .post()
        .uri(AuthControllerUriFactory.getVerifyEmailPath())
        .body(new VerifyEmailRequestDto(verificationToken.value()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();
  }

  public record TokenPair(String accessToken, String refreshToken) {}

  public TokenPair loginUser(final String email, final String password) {
    final LoginResponseDto loginResponse =
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
    return new TokenPair(loginResponse.accessToken(), loginResponse.refreshToken());
  }

  /**
   * Registers a new user, verifies their email, and logs them in.
   *
   * @param username Name of the account
   * @param email Email address of the user account
   * @param password Password of the user account
   * @return Access and Refresh Tokens for the logged-in user.
   */
  public TokenPair registerAndLoginUser(
      final String username, final String email, final String password) {
    registerAndVerifyNewUser(username, email, password);
    final FlowUtils.TokenPair loginResponse = loginUser(email, password);
    return new TokenPair(loginResponse.accessToken(), loginResponse.refreshToken());
  }

  public TokenPair refreshTokens(final String refreshToken) {
    final RefreshResponseDto refreshResponse =
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
    return new TokenPair(refreshResponse.accessToken(), refreshResponse.refreshToken());
  }

  public void logoutUser(final String refreshToken) {
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
}
