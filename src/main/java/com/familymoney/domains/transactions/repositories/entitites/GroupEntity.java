package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import java.time.Instant;
import javax.money.CurrencyUnit;
import lombok.Builder;

@Builder
public record GroupEntity(
    GroupId id,
    GroupName name,
    String description,
    CurrencyUnit currency,
    Instant createdAt,
    Instant updatedAt) {}
