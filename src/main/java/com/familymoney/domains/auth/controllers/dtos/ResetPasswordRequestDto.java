package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.auth.validation.ValidPasswordResetToken;
import com.familymoney.domains.user.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
    @NotBlank @ValidPasswordResetToken String token, @NotBlank @ValidPassword String newPassword) {}
