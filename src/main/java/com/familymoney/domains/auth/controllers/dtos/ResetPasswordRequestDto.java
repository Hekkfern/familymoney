package com.familymoney.domains.auth.controllers.dtos;

import com.familymoney.domains.auth.validations.ValidPasswordResetToken;
import com.familymoney.domains.users.validations.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
    @NotBlank @ValidPasswordResetToken String token, @NotBlank @ValidPassword String newPassword) {}
