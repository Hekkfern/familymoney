package com.familymoney.familymoney.controllers.dtos.user;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.Username;
import java.util.Optional;

public record UpdateUserRequestDto(
    Optional<Username> username, Optional<Email> email, Optional<Password> password) {}
