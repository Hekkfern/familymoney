package com.familymoney.familymoney.dtos.auth;

import com.familymoney.familymoney.validation.Password;
import com.familymoney.familymoney.validation.Username;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
    @NotBlank @Username String username,
    @NotBlank @Email String email,
    @NotBlank @Password String password) {}
