package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dbos.BalanceKeyDbo;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.mappers.BalanceJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.GroupBalances;
import com.familymoney.generated.tables.Groups;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultBalanceRepository implements BalanceRepository {

  private final DSLContext db;

  @Override
  public void create(final BalanceKeyDbo data) {
    final OrderedUsers orderedUsers = orderUsers(data.user1(), data.user2());
    db.insertInto(GroupBalances.GROUP_BALANCES)
        .columns(
            GroupBalances.GROUP_BALANCES.GROUP_ID,
            GroupBalances.GROUP_BALANCES.USER_ID_1,
            GroupBalances.GROUP_BALANCES.USER_ID_2)
        .values(data.groupId().value(), orderedUsers.lower().value(), orderedUsers.higher().value())
        .execute();
  }

  @Override
  public void setByKey(BalanceKeyDbo key, BigDecimal newBalance) {
    // TODO
  }

  @Transactional
  @Override
  public void incrementByKey(final BalanceKeyDbo key, final BigDecimal delta) {
    final OrderedUsers orderedUsers = orderUsers(key.user1(), key.user2());
    db.update(GroupBalances.GROUP_BALANCES)
        .set(GroupBalances.GROUP_BALANCES.AMOUNT, GroupBalances.GROUP_BALANCES.AMOUNT.plus(delta))
        .where(GroupBalances.GROUP_BALANCES.GROUP_ID.eq(key.groupId().value()))
        .and(GroupBalances.GROUP_BALANCES.USER_ID_1.eq(orderedUsers.lower().value()))
        .and(GroupBalances.GROUP_BALANCES.USER_ID_2.eq(orderedUsers.higher().value()))
        .execute();
  }

  @Override
  public Optional<BalanceEntity> findByKey(final BalanceKeyDbo key) {
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

  @Override
  public int deleteBalancesOfPurgeableGroups(final Instant deletedBefore, final int batchSize) {
    return db.deleteFrom(GroupBalances.GROUP_BALANCES)
        .where(
            DSL.row(
                    GroupBalances.GROUP_BALANCES.GROUP_ID,
                    GroupBalances.GROUP_BALANCES.USER_ID_1,
                    GroupBalances.GROUP_BALANCES.USER_ID_2)
                .in(
                    db.select(
                            GroupBalances.GROUP_BALANCES.GROUP_ID,
                            GroupBalances.GROUP_BALANCES.USER_ID_1,
                            GroupBalances.GROUP_BALANCES.USER_ID_2)
                        .from(GroupBalances.GROUP_BALANCES)
                        .join(Groups.GROUPS)
                        .on(Groups.GROUPS.ID.eq(GroupBalances.GROUP_BALANCES.GROUP_ID))
                        .where(
                            Groups.GROUPS.DELETED_AT.lt(
                                deletedBefore.atOffset(DEFAULT_TIMEZONE_OFFSET)))
                        .limit(batchSize)))
        .execute();
  }
}
