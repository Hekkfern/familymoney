package com.familymoney.familymoney.controllers.dtos.grouptransaction;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RemoveUserRequestDto(@NotNull UUID userId) {}
