package com.familymoney.domains.transactions.controllers.dtos;

import java.util.Map;
import java.util.UUID;
import org.javamoney.moneta.Money;

/**
 * DTO for the response of getting group balances.
 *
 * @param balances A map of user IDs to their respective balances. A positive balance indicates that
 *     the user is owed money, while a negative balance indicates that the user owes money.
 */
public record GetGroupBalancesResponseDto(Map<UUID, Money> balances) {}
