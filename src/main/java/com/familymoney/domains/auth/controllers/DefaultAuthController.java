package com.familymoney.domains.auth.controllers;

import com.familymoney.domains.auth.controllers.dtos.ForgotPasswordRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LoginRequestDto;
import com.familymoney.domains.auth.controllers.dtos.LoginResponseDto;
import com.familymoney.domains.auth.controllers.dtos.LogoutRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshResponseDto;
import com.familymoney.domains.auth.controllers.dtos.RefreshTokenRequestDto;
import com.familymoney.domains.auth.controllers.dtos.RegisterRequestDto;
import com.familymoney.domains.auth.controllers.dtos.ResendVerificationEmailRequestDto;
import com.familymoney.domains.auth.controllers.dtos.ResetPasswordRequestDto;
import com.familymoney.domains.auth.controllers.dtos.VerifyEmailRequestDto;
import com.familymoney.domains.auth.controllers.mappers.LoginResponseMapper;
import com.familymoney.domains.auth.controllers.mappers.RefreshResponseMapper;
import com.familymoney.domains.auth.services.AuthService;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.UserName;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultAuthController implements AuthController {

  private final AuthService authService;

  @Override
  public void register(final RegisterRequestDto request) {
    authService.registerUser(
        UserName.fromString(request.username()),
        Email.fromString(request.email()),
        Password.fromString(request.password()));
  }

  @Override
  public LoginResponseDto login(final LoginRequestDto request) {
    final TokenPair tokenPair =
        authService.loginUser(
            Email.fromString(request.email()), Password.fromString(request.password()));
    return LoginResponseMapper.toDto(tokenPair);
  }

  @Override
  public void verifyEmail(final VerifyEmailRequestDto request) {
    authService.verifyEmail(EmailVerificationToken.fromString(request.token()));
  }

  @Override
  public void resendVerificationEmail(final ResendVerificationEmailRequestDto request) {
    authService.resendVerificationEmail(Email.fromString(request.email()));
  }

  @Override
  public void forgotPassword(final ForgotPasswordRequestDto request) {
    authService.forgotPassword(Email.fromString(request.email()));
  }

  @Override
  public void resetPassword(final ResetPasswordRequestDto request) {
    authService.resetPassword(
        PasswordResetToken.fromString(request.token()), Password.fromString(request.newPassword()));
  }

  @Override
  public RefreshResponseDto refresh(final RefreshTokenRequestDto request) {
    final TokenPair tokenPair =
        authService.refreshTokens(RefreshToken.fromString(request.refreshToken()));
    return RefreshResponseMapper.toDto(tokenPair);
  }

  @Override
  public void logout(final LogoutRequestDto request) {
    authService.logoutUser(RefreshToken.fromString(request.refreshToken()));
  }
}
