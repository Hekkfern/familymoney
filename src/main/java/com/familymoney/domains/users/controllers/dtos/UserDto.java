package com.familymoney.domains.users.controllers.dtos;

import java.util.UUID;

public record UserDto(
    UUID id, String username, String email, boolean isEmailVerified, boolean isEnabled) {}
