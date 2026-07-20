package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.repositories.mappers.GroupJooqMapper;
import com.familymoney.domains.transactions.repositories.mappers.UserGroupJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.Groups;
import com.familymoney.generated.tables.UserGroups;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
  public Optional<GroupEntity> create(final CreateGroupDto data) {
    return db.insertInto(Groups.GROUPS)
        .columns(
            Groups.GROUPS.ID,
            Groups.GROUPS.NAME,
            Groups.GROUPS.DESCRIPTION,
            Groups.GROUPS.CURRENCY_CODE)
        .values(
            data.id().value(),
            data.name().value(),
            data.description(),
            data.currency().getCurrencyCode())
        .returning(
            Groups.GROUPS.ID,
            Groups.GROUPS.NAME,
            Groups.GROUPS.DESCRIPTION,
            Groups.GROUPS.CURRENCY_CODE,
            Groups.GROUPS.CREATED_AT,
            Groups.GROUPS.UPDATED_AT)
        .fetchOptional()
        .map(GroupJooqMapper::toEntity);
  }

  @Override
  public boolean updateById(final GroupId id, final UpdateGroupDto data) {
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
                    DSL.val(data.description() != null ? data.description() : null),
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
  public Page<GroupEntity> findByUserId(final UserId userId, final Pageable pageable) {
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
            .map(GroupJooqMapper::toEntity);

    return new PageImpl<>(data, pageable, safeTotal);
  }

  @Override
  public Optional<GroupEntity> findById(final GroupId id) {
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
        .map(GroupJooqMapper::toEntity);
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
  public Optional<UserGroupEntity> addUser(UserId userId, GroupId groupId) {
    return db.insertInto(UserGroups.USER_GROUPS)
        .columns(UserGroups.USER_GROUPS.USER_ID, UserGroups.USER_GROUPS.GROUP_ID)
        .values(userId.value(), groupId.value())
        .returning(
            UserGroups.USER_GROUPS.USER_ID,
            UserGroups.USER_GROUPS.GROUP_ID,
            UserGroups.USER_GROUPS.JOINED_AT)
        .fetchOptional()
        .map(UserGroupJooqMapper::toEntity);
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
