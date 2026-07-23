package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import java.time.Instant;
import javax.money.CurrencyUnit;

public record GroupEntity(
    GroupId id,
    GroupName name,
    Description description,
    CurrencyUnit currency,
    Instant createdAt,
    Instant updatedAt) {}
