package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;

public record CreatePaymentDto(
    PaymentId id,
    Description description,
    GroupId groupId,
    UserId createdBy,
    Instant doneAt,
    BigDecimal amount,
    UserId creditor,
    UserId debitor) {}
