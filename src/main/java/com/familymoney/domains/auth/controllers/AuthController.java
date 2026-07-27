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
import com.familymoney.domains.auth.controllers.mappers.LoginResponseMapper;
import com.familymoney.domains.auth.controllers.mappers.RefreshResponseMapper;
import com.familymoney.domains.auth.services.IAuthService;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.UserName;
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
