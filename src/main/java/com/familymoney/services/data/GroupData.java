package com.familymoney.services.data;

import com.familymoney.types.*;
import java.time.Instant;
import javax.money.CurrencyUnit;
import lombok.Builder;

@Builder
public record GroupData(
    GroupId id,
    GroupName name,
    String description,
    CurrencyUnit currency,
    Instant createdAt) {}
