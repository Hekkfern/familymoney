package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateBalanceDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateBalanceDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreateBalanceException;
import com.familymoney.domains.transactions.repositories.exceptions.UpdateBalanceException;
import com.familymoney.domains.transactions.repositories.mappers.BalanceJooqMapper;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.generated.tables.GroupBalances;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultBalanceRepository implements BalanceRepository {

  private final DSLContext db;

  @Transactional
  @Override
  public void create(final CreateBalanceDto data) {
    final int balancesCreated =
        db.insertInto(GroupBalances.GROUP_BALANCES)
            .columns(
                GroupBalances.GROUP_BALANCES.ID,
                GroupBalances.GROUP_BALANCES.GROUP_ID,
                GroupBalances.GROUP_BALANCES.AMOUNT,
                GroupBalances.GROUP_BALANCES.CURRENCY_CODE,
                GroupBalances.GROUP_BALANCES.USER_ID_1,
                GroupBalances.GROUP_BALANCES.USER_ID_2)
            .values(
                data.id().value(),
                data.groupId().value(),
                data.amount().getNumber().numberValue(BigDecimal.class),
                data.amount().getCurrency().getCurrencyCode(),
                data.user1().value(),
                data.user2().value())
            .execute();
    if (balancesCreated != 1) {
      throw new CreateBalanceException(
          "Could not create payment with ID: %s".formatted(data.id().value()));
    }
  }

  @Transactional
  @Override
  public void updateById(final BalanceId id, final UpdateBalanceDto dto) {
    if (dto.isEmpty()) {
      return;
    }

    final Map<Field<?>, Object> values = new LinkedHashMap<>();
    if (dto.money() != null) {
      values.put(
          GroupBalances.GROUP_BALANCES.AMOUNT,
          dto.money().getNumber().numberValue(BigDecimal.class));
      values.put(
          GroupBalances.GROUP_BALANCES.CURRENCY_CODE, dto.money().getCurrency().getCurrencyCode());
    }
    if (dto.user1() != null) {
      values.put(GroupBalances.GROUP_BALANCES.USER_ID_1, dto.user1().value());
    }
    if (dto.user2() != null) {
      values.put(GroupBalances.GROUP_BALANCES.USER_ID_2, dto.user2().value());
    }

    final int updatedRows =
        db.update(GroupBalances.GROUP_BALANCES)
            .set(values)
            .where(GroupBalances.GROUP_BALANCES.ID.eq(id.value()))
            .execute();
    if (updatedRows != 1) {
      throw new UpdateBalanceException("Could not update balance ID: %s".formatted(id.value()));
    }
  }

  @Override
  public Optional<BalanceEntity> findById(final BalanceId id) {
    return db.selectFrom(GroupBalances.GROUP_BALANCES)
        .where(GroupBalances.GROUP_BALANCES.ID.eq(id.value()))
        .fetchOptional(BalanceJooqMapper::toEntity);
  }

  @Override
  public List<BalanceEntity> findByGroupId(final GroupId groupId) {
    return db.selectFrom(GroupBalances.GROUP_BALANCES)
        .where(GroupBalances.GROUP_BALANCES.GROUP_ID.eq(groupId.value()))
        .fetch(BalanceJooqMapper::toEntity);
  }
}
