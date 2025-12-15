package com.familymoney.familymoney.controllers;

import com.familymoney.familymoney.controllers.dtos.auth.ForgotPasswordRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.LoginRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.LoginResponseDto;
import com.familymoney.familymoney.controllers.dtos.auth.LogoutRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.RefreshResponseDto;
import com.familymoney.familymoney.controllers.dtos.auth.RefreshTokenRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.RegisterRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.ResendVerificationEmailRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.ResetPasswordRequestDto;
import com.familymoney.familymoney.services.IAuthService;
import com.familymoney.familymoney.types.EmailVerificationToken;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements IAuthController {

  private final IAuthService authService;

  @Override
  public void register(RegisterRequestDto request) {
    authService.registerUser(request.username(), request.email(), request.password());
  }

  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    val tokenPair = authService.loginUser(request.email(), request.password());
    return new LoginResponseDto(tokenPair.accessToken(), tokenPair.refreshToken());
  }

  @Override
  public void verifyEmail(EmailVerificationToken token) {
    authService.verifyEmail(token);
  }

  @Override
  public void resendVerificationEmail(ResendVerificationEmailRequestDto request) {
    authService.resendVerificationEmail(request.email());
  }

  @Override
  public void forgotPassword(ForgotPasswordRequestDto request) {
    authService.forgotPassword(request.email());
  }

  @Override
  public void resetPassword(ResetPasswordRequestDto request) {
    authService.resetPassword(request.token(), request.newPassword());
  }

  @Override
  public RefreshResponseDto refresh(RefreshTokenRequestDto request) {
    val tokenPair = authService.refreshTokens(request.refreshToken());
    return new RefreshResponseDto(tokenPair.accessToken(), tokenPair.refreshToken());
  }

  @Override
  public void logout(LogoutRequestDto request) {
    authService.logoutUser(request.refreshToken());
  }
}
