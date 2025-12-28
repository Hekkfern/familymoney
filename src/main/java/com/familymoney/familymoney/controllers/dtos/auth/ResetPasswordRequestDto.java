package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.validation.ValidPassword;
import com.familymoney.familymoney.validation.ValidPasswordResetToken;
import jakarta.validation.constraints.NotNull;

public record ResetPasswordRequestDto(
    @NotNull @ValidPasswordResetToken String token, @NotNull @ValidPassword String newPassword) {}
