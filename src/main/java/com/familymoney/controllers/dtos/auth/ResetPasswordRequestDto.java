package com.familymoney.controllers.dtos.auth;

import com.familymoney.validation.ValidPassword;
import com.familymoney.validation.ValidPasswordResetToken;
import jakarta.validation.constraints.NotNull;

public record ResetPasswordRequestDto(
    @NotNull @ValidPasswordResetToken String token, @NotNull @ValidPassword String newPassword) {}
