package com.familymoney.familymoney.dtos.auth;

import com.familymoney.familymoney.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@NotBlank @Email String email, @NotBlank @Password String password) {}
