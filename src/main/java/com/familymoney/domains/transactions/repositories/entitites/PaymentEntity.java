package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEntity(
    PaymentId id,
    Description description,
    BigDecimal amount,
    UserId creditor,
    UserId debitor,
    GroupId groupId,
    Instant doneAt) {}
