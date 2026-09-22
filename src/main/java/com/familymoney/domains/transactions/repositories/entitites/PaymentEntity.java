package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;

public record PaymentEntity(
    PaymentId id,
    Description description,
    Money amount,
    UserId creditor,
    UserId debitor,
    GroupId groupId,
    UserId createdBy,
    Instant doneAt,
    Instant createdAt,
    Instant updatedAt) {}
