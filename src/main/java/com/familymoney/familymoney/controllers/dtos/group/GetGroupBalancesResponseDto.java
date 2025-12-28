package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.types.UserId;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;

@Builder
public record GetGroupBalancesResponseDto(Map<UserId, BigDecimal> balances) {}
