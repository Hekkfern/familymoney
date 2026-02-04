package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.generated.tables.Groups;
import com.familymoney.familymoney.generated.tables.UserGroups;
import com.familymoney.familymoney.repositories.dbos.GroupDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateGroupDbo;
import com.familymoney.familymoney.repositories.dbos.UserGroupDbo;
import com.familymoney.familymoney.repositories.mappers.GroupJooqMapper;
import com.familymoney.familymoney.repositories.mappers.UserGroupJooqMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRepository implements IGroupRepository {

  private final DSLContext db;

  @Override
  public Optional<GroupDbo> create(
      final GroupName name,
      final String description,
      final CurrencyUnit currency) {
    return db.insertInto(Groups.GROUPS)
        .columns(Groups.GROUPS.NAME, Groups.GROUPS.DESCRIPTION, Groups.GROUPS.CURRENCY_CODE)
        .values(name.value(), description, currency.getCurrencyCode())
        .returning(
            Groups.GROUPS.ID,
            Groups.GROUPS.NAME,
            Groups.GROUPS.DESCRIPTION,
            Groups.GROUPS.CURRENCY_CODE,
            Groups.GROUPS.CREATED_AT,
            Groups.GROUPS.UPDATED_AT)
        .fetchOptional()
        .map(GroupJooqMapper::toDbo);
  }

  @Override
  public boolean updateById(final GroupId id, final UpdateGroupDbo data) {
    val rowsAffected =
        db.update(Groups.GROUPS)
            .set(
                Groups.GROUPS.NAME,
                DSL.coalesce(
                    DSL.val(data.getName() != null ? data.getName().value() : null),
                    Groups.GROUPS.NAME))
            .set(
                Groups.GROUPS.DESCRIPTION,
                DSL.coalesce(
                    DSL.val(data.getDescription() != null ? data.getDescription() : null),
                    Groups.GROUPS.DESCRIPTION))
            .where(Groups.GROUPS.ID.eq(id.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(final GroupId id) {
    val rowsAffected =
        db.deleteFrom(Groups.GROUPS).where(Groups.GROUPS.ID.eq(id.value())).execute();
    return rowsAffected > 0;
  }

  @Override
  public Page<GroupDbo> findByUserId(final UserId userId, final Pageable pageable) {
    val total =
        db.selectCount()
            .from(UserGroups.USER_GROUPS)
            .where(UserGroups.USER_GROUPS.USER_ID.eq(userId.value()))
            .fetchOne(0, Long.class);
    val safeTotal = total != null ? total : 0L;

    val data =
        db.select(
                Groups.GROUPS.ID,
                Groups.GROUPS.NAME,
                Groups.GROUPS.DESCRIPTION,
                Groups.GROUPS.CURRENCY_CODE,
                Groups.GROUPS.CREATED_AT,
                Groups.GROUPS.UPDATED_AT)
            .from(UserGroups.USER_GROUPS)
            .join(Groups.GROUPS)
            .on(Groups.GROUPS.ID.eq(UserGroups.USER_GROUPS.GROUP_ID))
            .where(UserGroups.USER_GROUPS.USER_ID.eq(userId.value()))
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch()
            .map(GroupJooqMapper::toDbo);

    return new PageImpl<>(data, pageable, safeTotal);
  }

  @Override
  public Optional<GroupDbo> findById(final GroupId id) {
    return db.select(
            Groups.GROUPS.ID,
            Groups.GROUPS.NAME,
            Groups.GROUPS.DESCRIPTION,
            Groups.GROUPS.CURRENCY_CODE,
            Groups.GROUPS.CREATED_AT,
            Groups.GROUPS.UPDATED_AT)
        .from(Groups.GROUPS)
        .where(Groups.GROUPS.ID.eq(id.value()))
        .fetchOptional()
        .map(GroupJooqMapper::toDbo);
  }

  @Override
  public boolean existsById(GroupId id) {
    return db.fetchExists(
        db.selectOne().from(Groups.GROUPS).where(Groups.GROUPS.ID.eq(id.value())));
  }

  @Override
  public List<UserId> findUserIdsByGroupId(final GroupId id) {
    return db
        .select(UserGroups.USER_GROUPS.USER_ID)
        .from(UserGroups.USER_GROUPS)
        .where(UserGroups.USER_GROUPS.GROUP_ID.eq(id.value()))
        .fetch()
        .stream()
        .map(r -> r.get(UserGroups.USER_GROUPS.USER_ID))
        .filter(Objects::nonNull)
        .map(UserId::fromUuid)
        .toList();
  }

  @Override
  public boolean isUserInGroup(final UserId userId, final GroupId groupId) {
    return db.fetchExists(
        db.selectOne()
            .from(UserGroups.USER_GROUPS)
            .where(
                UserGroups.USER_GROUPS
                    .USER_ID
                    .eq(userId.value())
                    .and(UserGroups.USER_GROUPS.GROUP_ID.eq(groupId.value()))));
  }

  @Override
  public Optional<UserGroupDbo> addUser(UserId userId, GroupId groupId) {
    return db.insertInto(UserGroups.USER_GROUPS)
        .columns(UserGroups.USER_GROUPS.USER_ID, UserGroups.USER_GROUPS.GROUP_ID)
        .values(userId.value(), groupId.value())
        .returning(
            UserGroups.USER_GROUPS.USER_ID,
            UserGroups.USER_GROUPS.GROUP_ID,
            UserGroups.USER_GROUPS.JOINED_AT)
        .fetchOptional()
        .map(UserGroupJooqMapper::toDbo);
  }

  @Override
  public boolean deleteUser(UserId userId, GroupId groupId) {
    val rowsAffected =
        db.deleteFrom(UserGroups.USER_GROUPS)
            .where(
                UserGroups.USER_GROUPS
                    .USER_ID
                    .eq(userId.value())
                    .and(UserGroups.USER_GROUPS.GROUP_ID.eq(groupId.value())))
            .execute();
    return rowsAffected > 0;
  }
}
