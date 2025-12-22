package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import lombok.Builder;
import org.javamoney.moneta.Money;

@Builder
public record TransactionDbo(
    TransactionId id,
    String description,
    GroupId groupId,
    Money amount,
    UserId lender,
    UserId borrower,
    Instant createdAt,
    Instant updatedAt) {}
