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
import com.familymoney.familymoney.validation.EmailVerificationToken;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface IAuthController {

  @PostMapping("register")
  void register(@RequestBody @Valid RegisterRequestDto request);

  @PostMapping("login")
  LoginResponseDto login(@RequestBody @Valid LoginRequestDto request);

  @GetMapping("verify-email/{token}")
  void verifyEmail(@PathVariable @EmailVerificationToken String token);

  @PostMapping("verify-email/resend")
  void resendVerificationEmail(@RequestBody @Valid ResendVerificationEmailRequestDto request);

  @PostMapping("forgot-password")
  void forgotPassword(@RequestBody @Valid ForgotPasswordRequestDto request);

  @PostMapping("reset-password")
  void resetPassword(@RequestBody @Valid ResetPasswordRequestDto request);

  @PostMapping("refresh")
  RefreshResponseDto refresh(@RequestBody @Valid RefreshTokenRequestDto request);

  @PostMapping("logout")
  void logout(@RequestBody @Valid LogoutRequestDto request);
}
