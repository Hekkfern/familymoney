package com.familymoney.controllers.impl;

import com.familymoney.controllers.IAuthController;
import com.familymoney.controllers.dtos.auth.ForgotPasswordRequestDto;
import com.familymoney.controllers.dtos.auth.LoginRequestDto;
import com.familymoney.controllers.dtos.auth.LoginResponseDto;
import com.familymoney.controllers.dtos.auth.LogoutRequestDto;
import com.familymoney.controllers.dtos.auth.RefreshResponseDto;
import com.familymoney.controllers.dtos.auth.RefreshTokenRequestDto;
import com.familymoney.controllers.dtos.auth.RegisterRequestDto;
import com.familymoney.controllers.dtos.auth.ResendVerificationEmailRequestDto;
import com.familymoney.controllers.dtos.auth.ResetPasswordRequestDto;
import com.familymoney.controllers.mappers.auth.LoginResponseMapper;
import com.familymoney.controllers.mappers.auth.RefreshResponseMapper;
import com.familymoney.services.IAuthService;
import com.familymoney.types.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements IAuthController {

  private final IAuthService authService;
  private final LoginResponseMapper loginResponseMapper;
  private final RefreshResponseMapper refreshResponseMapper;

  @Override
  public void register(RegisterRequestDto request) {
    authService.registerUser(
        UserName.fromString(request.username()),
        Email.fromString(request.email()),
        Password.fromString(request.password()));
  }

  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    val tokenPair =
        authService.loginUser(
            Email.fromString(request.email()), Password.fromString(request.password()));
    return loginResponseMapper.toDto(tokenPair);
  }

  @Override
  public void verifyEmail(String token) {
    authService.verifyEmail(EmailVerificationToken.fromString(token));
  }

  @Override
  public void resendVerificationEmail(ResendVerificationEmailRequestDto request) {
    authService.resendVerificationEmail(Email.fromString(request.email()));
  }

  @Override
  public void forgotPassword(ForgotPasswordRequestDto request) {
    authService.forgotPassword(Email.fromString(request.email()));
  }

  @Override
  public void resetPassword(ResetPasswordRequestDto request) {
    authService.resetPassword(
        PasswordResetToken.fromString(request.token()), Password.fromString(request.newPassword()));
  }

  @Override
  public RefreshResponseDto refresh(RefreshTokenRequestDto request) {
    val tokenPair = authService.refreshTokens(RefreshToken.fromString(request.refreshToken()));
    return refreshResponseMapper.toDto(tokenPair);
  }

  @Override
  public void logout(LogoutRequestDto request) {
    authService.logoutUser(RefreshToken.fromString(request.refreshToken()));
  }
}
