package com.familymoney.domains.transactions.controllers.dtos;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import org.javamoney.moneta.Money;

@Builder
public record GetGroupBalancesResponseDto(Map<UUID, Money> balances) {}
