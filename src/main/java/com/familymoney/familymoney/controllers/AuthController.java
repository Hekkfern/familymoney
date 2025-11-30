package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.dtos.auth.ForgotPasswordRequestDto;
import com.familymoney.familymoney.dtos.auth.LoginRequestDto;
import com.familymoney.familymoney.dtos.auth.LoginResponseDto;
import com.familymoney.familymoney.dtos.auth.LogoutRequestDto;
import com.familymoney.familymoney.dtos.auth.RefreshResponseDto;
import com.familymoney.familymoney.dtos.auth.RefreshTokenRequestDto;
import com.familymoney.familymoney.dtos.auth.RegisterRequestDto;
import com.familymoney.familymoney.dtos.auth.ResendVerificationEmailRequestDto;
import com.familymoney.familymoney.dtos.auth.ResetPasswordRequestDto;
import com.familymoney.familymoney.services.IAuthService;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.Username;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements IAuthController {

  private final IAuthService authService;

  @Override
  public void register(RegisterRequestDto request) {
    authService.registerUser(
        new Username(request.username()),
        new Email(request.email()),
        new Password(request.password()));
  }

  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    val tokenPair =
        authService.loginUser(new Email(request.email()), new Password(request.password()));
    return new LoginResponseDto(tokenPair.accessToken().value(), tokenPair.refreshToken().value());
  }

  @Override
  public void verifyEmail(String token) {
    authService.verifyEmail(new EmailVerificationToken(token));
  }

  @Override
  public void resendVerificationEmail(ResendVerificationEmailRequestDto request) {
    authService.resendVerificationEmail(new Email(request.email()));
  }

  @Override
  public void forgotPassword(ForgotPasswordRequestDto request) {
    authService.forgotPassword(new Email(request.email()));
  }

  @Override
  public void resetPassword(ResetPasswordRequestDto request) {
    authService.resetPassword(
        new EmailVerificationToken(request.token()), new Password(request.newPassword()));
  }

  @Override
  public RefreshResponseDto refresh(RefreshTokenRequestDto request) {
    val tokenPair = authService.refreshTokens(new RefreshToken(request.refreshToken()));
    return new RefreshResponseDto(
        tokenPair.accessToken().value(), tokenPair.refreshToken().value());
  }

  @Override
  public void logout(LogoutRequestDto request) {
    authService.logoutUser(new RefreshToken(request.refreshToken()));
  }
}
