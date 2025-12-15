package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.Username;

public record RegisterRequestDto(Username username, Email email, Password password) {}
