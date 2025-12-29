package com.familymoney.familymoney.controllers.dtos.group;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RemoveUserRequestDto(@NotNull UUID userId) {}
