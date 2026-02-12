package com.familymoney.familymoney.repositories.dtos;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;

/**
 * DTO for creating a new transaction record in the database
 *
 * @param description Textual description for the transaction. May be empty.
 * @param groupId ID of the group to which the transaction belongs.
 * @param amount The monetary amount of the transaction. Must be positive.
 * @param lender ID of the user who lent the money.
 * @param borrower ID of the user who borrowed the money. Must be different from lender.
 * @param doneAt The timestamp indicating when the transaction was completed.
 */
@Builder
public record CreateTransactionDto(
    TransactionId id,
    String description,
    GroupId groupId,
    Money amount,
    UserId lender,
    UserId borrower,
    Instant doneAt) {
    public CreateTransactionDto{
        assert amount.isGreaterThan(Money.zero(amount.getCurrency())) : "Amount must be positive";
        assert !lender.equals(borrower) : "Lender and borrower must be different users";
    }
}
