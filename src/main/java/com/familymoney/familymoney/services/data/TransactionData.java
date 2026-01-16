package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;

public record TransactionData(
    TransactionId id,
    String description,
    GroupId groupId,
    Money amount,
    UserId lender,
    UserId borrower,
    Instant doneAt,
    Instant createdAt) {}
