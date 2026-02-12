package com.familymoney.familymoney.repositories.dtos;

import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import lombok.Builder;

import javax.money.CurrencyUnit;

/**
 * DTO for creating a new balance record in the database.
 *
 * @param id the unique identifier for the balance record.
 * @param groupId the identifier of the group for which the balance is being created
 * @param user1 the identifier of the first user involved in the balance
 * @param user2 the identifier of the second user involved in the balance
 * @param currency the currency unit for the balance amount (e.g., USD, EUR). This indicates the
 *     currency in which the balance amount is denominated.
 */
@Builder
public record CreateBalanceDto(
    BalanceId id, GroupId groupId, UserId user1, UserId user2, CurrencyUnit currency) {}
