package com.familymoney.domains.transactions.controllers.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendInvitationTokenRequestDto(@NotNull UUID userId) {}
