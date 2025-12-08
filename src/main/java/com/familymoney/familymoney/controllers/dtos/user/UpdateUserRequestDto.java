package com.familymoney.familymoney.controllers.dtos.user;

import com.familymoney.familymoney.validation.Password;
import com.familymoney.familymoney.validation.Username;
import jakarta.validation.constraints.Email;

public record UpdateUserRequestDto(
    @Username String username, @Email String email, @Password String password) {}
