package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.UUID;
import javax.money.CurrencyUnit;

public record GroupDbo(
    UUID id,
    GroupName name,
    String description,
    CurrencyUnit currency,
    UserId createdBy,
    Instant createdAt,
    Instant updatedAt) {}
