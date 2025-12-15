package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.PasswordResetToken;

public record ResetPasswordRequestDto(PasswordResetToken token, Password newPassword) {}
