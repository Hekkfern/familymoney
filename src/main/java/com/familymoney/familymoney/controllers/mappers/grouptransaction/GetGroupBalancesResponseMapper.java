package com.familymoney.familymoney.controllers.mappers.grouptransaction;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.GetGroupBalancesResponseDto;
import com.familymoney.familymoney.types.UserId;
import java.util.Map;
import java.util.stream.Collectors;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;

@Component
public class GetGroupBalancesResponseMapper {

  public GetGroupBalancesResponseDto toDto(Map<UserId, Money> balanceMap) {
    var balances =
        balanceMap.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().value(), Map.Entry::getValue));
    return GetGroupBalancesResponseDto.builder().balances(balances).build();
  }
}
