package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.auth.validation.ValidPasswordResetToken;
import com.familymoney.domains.user.validation.ValidPassword;
import jakarta.validation.constraints.NotNull;

public record ResetPasswordRequestDto(
    @NotNull @ValidPasswordResetToken String token, @NotNull @ValidPassword String newPassword) {}
