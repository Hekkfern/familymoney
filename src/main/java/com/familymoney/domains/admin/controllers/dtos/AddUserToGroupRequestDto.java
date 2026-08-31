package com.familymoney.domains.admin.controllers.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddUserToGroupRequestDto(@NotNull UUID userId) {}
