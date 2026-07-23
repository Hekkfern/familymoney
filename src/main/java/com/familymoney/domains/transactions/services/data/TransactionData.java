package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;

public record TransactionData(
    TransactionId id,
    Description description,
    GroupId groupId,
    Money amount,
    UserId from,
    UserId to,
    Instant doneAt,
    Instant createdAt) {}
