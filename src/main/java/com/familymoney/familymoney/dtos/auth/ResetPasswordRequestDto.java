package com.familymoney.familymoney.dtos.auth;

import com.familymoney.familymoney.validation.EmailVerificationToken;
import com.familymoney.familymoney.validation.Password;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
    @NotBlank @EmailVerificationToken String token, @NotBlank @Password String newPassword) {}
