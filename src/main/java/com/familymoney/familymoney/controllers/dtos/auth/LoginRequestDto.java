package com.familymoney.familymoney.controllers.dtos.auth;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;

public record LoginRequestDto(Email email, Password password) {}
