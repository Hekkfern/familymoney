package com.familymoney.domains.transactions.services.data;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.javamoney.moneta.Money;

public record ExpenseData(
    ExpenseId id,
    GroupId groupId,
    Description description,
    Money totalAmount,
    Instant doneAt,
    Map<UserId, BigDecimal> shares,
    Map<UserId, BigDecimal> payers) {}
