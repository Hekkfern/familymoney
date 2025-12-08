package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.Username;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record UpdateUserData(
    @NonNull Optional<Username> username,
    @NonNull Optional<Email> email,
    @NonNull Optional<Password> password) {}
