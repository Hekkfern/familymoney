package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.user.types.UserId;
import javax.money.CurrencyUnit;
import lombok.Builder;

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
