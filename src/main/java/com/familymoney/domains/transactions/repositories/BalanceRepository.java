package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateBalanceDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateBalanceDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.mappers.BalanceJooqMapper;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.Balances;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BalanceRepository implements IBalanceRepository {

  private final DSLContext db;

  @Override
  public Optional<BalanceEntity> create(final CreateBalanceDto data) {
    return db.insertInto(Balances.BALANCES)
        .columns(
            Balances.BALANCES.ID,
            Balances.BALANCES.GROUP_ID,
            Balances.BALANCES.AMOUNT,
            Balances.BALANCES.CURRENCY_CODE,
            Balances.BALANCES.USER_ID_1,
            Balances.BALANCES.USER_ID_2)
        .values(
            data.id().value(),
            data.groupId().value(),
            data.amount().getNumber().numberValue(BigDecimal.class),
            data.amount().getCurrency().getCurrencyCode(),
            data.user1().value(),
            data.user2().value())
        .returning(
            Balances.BALANCES.ID,
            Balances.BALANCES.GROUP_ID,
            Balances.BALANCES.AMOUNT,
            Balances.BALANCES.CURRENCY_CODE,
            Balances.BALANCES.USER_ID_1,
            Balances.BALANCES.USER_ID_2)
        .fetchOptional()
        .map(BalanceJooqMapper::toEntity);
  }

  @Override
  public List<BalanceEntity> findByGroup(GroupId groupId) {
    return db.select(
            Balances.BALANCES.ID,
            Balances.BALANCES.GROUP_ID,
            Balances.BALANCES.AMOUNT,
            Balances.BALANCES.CURRENCY_CODE,
            Balances.BALANCES.USER_ID_1,
            Balances.BALANCES.USER_ID_2)
        .from(Balances.BALANCES)
        .where(Balances.BALANCES.GROUP_ID.eq(groupId.value()))
        .fetch()
        .map(BalanceJooqMapper::toEntity);
  }

  @Override
  public List<BalanceEntity> findByUserAndGroup(final UserId userId, final GroupId groupId) {
    return db.select(
            Balances.BALANCES.ID,
            Balances.BALANCES.GROUP_ID,
            Balances.BALANCES.AMOUNT,
            Balances.BALANCES.CURRENCY_CODE,
            Balances.BALANCES.USER_ID_1,
            Balances.BALANCES.USER_ID_2)
        .from(Balances.BALANCES)
        .where(
            Balances.BALANCES
                .GROUP_ID
                .eq(groupId.value())
                .and(
                    Balances.BALANCES
                        .USER_ID_1
                        .eq(userId.value())
                        .or(Balances.BALANCES.USER_ID_2.eq(userId.value()))))
        .fetch()
        .map(BalanceJooqMapper::toEntity);
  }

  @Override
  public boolean updateById(final BalanceId id, final UpdateBalanceDto data) {
    val amountValue =
        data.money() != null ? data.money().getNumber().numberValue(BigDecimal.class) : null;
    val currencyValue = data.money() != null ? data.money().getCurrency().getCurrencyCode() : null;
    val user1Value = data.user1() != null ? data.user1().value() : null;
    val user2Value = data.user2() != null ? data.user2().value() : null;

    int rowsAffected =
        db.update(Balances.BALANCES)
            .set(
                Balances.BALANCES.AMOUNT,
                DSL.coalesce(DSL.val(amountValue), Balances.BALANCES.AMOUNT))
            .set(
                Balances.BALANCES.CURRENCY_CODE,
                DSL.coalesce(DSL.val(currencyValue), Balances.BALANCES.CURRENCY_CODE))
            .set(
                Balances.BALANCES.USER_ID_1,
                DSL.coalesce(DSL.val(user1Value), Balances.BALANCES.USER_ID_1))
            .set(
                Balances.BALANCES.USER_ID_2,
                DSL.coalesce(DSL.val(user2Value), Balances.BALANCES.USER_ID_2))
            .where(Balances.BALANCES.ID.eq(id.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public Optional<BalanceEntity> findById(final BalanceId id) {
    return db.select(
            Balances.BALANCES.ID,
            Balances.BALANCES.GROUP_ID,
            Balances.BALANCES.AMOUNT,
            Balances.BALANCES.CURRENCY_CODE,
            Balances.BALANCES.USER_ID_1,
            Balances.BALANCES.USER_ID_2)
        .from(Balances.BALANCES)
        .where(Balances.BALANCES.ID.eq(id.value()))
        .fetchOptional()
        .map(BalanceJooqMapper::toEntity);
  }
}
