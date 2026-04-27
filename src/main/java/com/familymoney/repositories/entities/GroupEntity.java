package com.familymoney.repositories.entities;

import com.familymoney.types.GroupId;
import com.familymoney.types.GroupName;

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
