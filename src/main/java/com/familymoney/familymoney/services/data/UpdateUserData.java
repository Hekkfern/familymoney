package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.Username;
import java.util.Optional;

public record UpdateUserData(
    Optional<Username> username, Optional<Email> email, Optional<Password> password) {}
