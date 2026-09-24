package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.BalanceKey;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreateBalanceException;
import com.familymoney.domains.transactions.repositories.exceptions.UpdateBalanceException;
import com.familymoney.domains.transactions.repositories.mappers.BalanceJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.GroupBalances;
import com.familymoney.generated.tables.Groups;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultBalanceRepository implements BalanceRepository {

  private final DSLContext db;

  @Override
  public void create(final BalanceKey data) {
    final OrderedUsers orderedUsers = orderUsers(data.user1(), data.user2());

    final int balancesCreated =
        db.insertInto(GroupBalances.GROUP_BALANCES)
            .columns(
                GroupBalances.GROUP_BALANCES.GROUP_ID,
                GroupBalances.GROUP_BALANCES.USER_ID_1,
                GroupBalances.GROUP_BALANCES.USER_ID_2)
            .values(
                data.groupId().value(), orderedUsers.lower().value(), orderedUsers.higher().value())
            .execute();
    if (balancesCreated != 1) {
      throw new CreateBalanceException(
          "Could not create balance in Group '%s', between users '%s' and '%s'"
              .formatted(data.groupId().value(), data.user1().value(), data.user2().value()));
    }
  }

  @Transactional
  @Override
  public void incrementByKey(final BalanceKey key, final Money delta) {
    final String currencyCode =
        db.select(Groups.GROUPS.CURRENCY_CODE)
            .from(Groups.GROUPS)
            .where(Groups.GROUPS.ID.eq(key.groupId().value()))
            .fetchOptional(Groups.GROUPS.CURRENCY_CODE)
            .orElseThrow(
                () ->
                    new UpdateBalanceException(
                        "Could not find group with ID: %s".formatted(key.groupId().value())));
    if (!currencyCode.equals(delta.getCurrency().getCurrencyCode())) {
      throw new UpdateBalanceException(
          "Balance delta in Group '%s' must use currency: %s"
              .formatted(key.groupId().value(), currencyCode));
    }

    final OrderedUsers orderedUsers = orderUsers(key.user1(), key.user2());
    final BigDecimal deltaValue = delta.getNumber().numberValue(BigDecimal.class);
    final int updatedRows =
        db.update(GroupBalances.GROUP_BALANCES)
            .set(
                GroupBalances.GROUP_BALANCES.AMOUNT,
                GroupBalances.GROUP_BALANCES.AMOUNT.plus(deltaValue))
            .where(GroupBalances.GROUP_BALANCES.GROUP_ID.eq(key.groupId().value()))
            .and(GroupBalances.GROUP_BALANCES.USER_ID_1.eq(orderedUsers.lower().value()))
            .and(GroupBalances.GROUP_BALANCES.USER_ID_2.eq(orderedUsers.higher().value()))
            .execute();
    if (updatedRows != 1) {
      throw new UpdateBalanceException(
          "Could not update balance in Group '%s', between users '%s' and '%s'"
              .formatted(key.groupId().value(), key.user1().value(), key.user2().value()));
    }
  }

  @Override
  public Optional<BalanceEntity> findByKey(final BalanceKey key) {
    final OrderedUsers orderedUsers = orderUsers(key.user1(), key.user2());
    return db.select(GroupBalances.GROUP_BALANCES.fields())
        .select(Groups.GROUPS.CURRENCY_CODE)
        .from(GroupBalances.GROUP_BALANCES)
        .join(Groups.GROUPS)
        .on(Groups.GROUPS.ID.eq(GroupBalances.GROUP_BALANCES.GROUP_ID))
        .where(GroupBalances.GROUP_BALANCES.GROUP_ID.eq(key.groupId().value()))
        .and(GroupBalances.GROUP_BALANCES.USER_ID_1.eq(orderedUsers.lower().value()))
        .and(GroupBalances.GROUP_BALANCES.USER_ID_2.eq(orderedUsers.higher().value()))
        .fetchOptional(BalanceJooqMapper::toEntity);
  }

  @Override
  public List<BalanceEntity> findByGroupId(final GroupId groupId) {
    return db.select(GroupBalances.GROUP_BALANCES.fields())
        .select(Groups.GROUPS.CURRENCY_CODE)
        .from(GroupBalances.GROUP_BALANCES)
        .join(Groups.GROUPS)
        .on(Groups.GROUPS.ID.eq(GroupBalances.GROUP_BALANCES.GROUP_ID))
        .where(GroupBalances.GROUP_BALANCES.GROUP_ID.eq(groupId.value()))
        .fetch(BalanceJooqMapper::toEntity);
  }

  private static OrderedUsers orderUsers(final UserId user1, final UserId user2) {
    final boolean isUser1Lower = user1.value().compareTo(user2.value()) <= 0;
    return isUser1Lower ? new OrderedUsers(user1, user2) : new OrderedUsers(user2, user1);
  }

  private record OrderedUsers(UserId lower, UserId higher) {}
}
