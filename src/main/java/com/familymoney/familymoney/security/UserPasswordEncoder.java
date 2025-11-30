package com.familymoney.familymoney.security;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordEncoder {

    private final int STRENGTH = 12;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(STRENGTH);

    @NonNull
    public String encode(@NonNull String rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    public boolean verify(@NonNull String rawPassword, @NonNull String hashedPassword) {
        return bcrypt.matches(rawPassword, hashedPassword);
    }
}
