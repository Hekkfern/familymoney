package com.familymoney.domains.transactions.controllers.dtos;

import java.util.Map;
import java.util.UUID;
import org.javamoney.moneta.Money;

public record GetGroupBalancesResponseDto(Map<UUID, Money> balances) {}
