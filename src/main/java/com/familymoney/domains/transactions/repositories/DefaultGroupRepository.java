package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInGroupException;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.mappers.GroupJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.Keys;
import com.familymoney.generated.tables.Groups;
import com.familymoney.generated.tables.UserGroups;
import com.familymoney.utils.ConstraintViolationUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupRepository implements GroupRepository {

  private final DSLContext db;

  @Override
  public void create(final CreateGroupDto dto) {
    try {
      db.insertInto(Groups.GROUPS)
          .columns(
              Groups.GROUPS.ID,
              Groups.GROUPS.NAME,
              Groups.GROUPS.DESCRIPTION,
              Groups.GROUPS.CURRENCY_CODE,
              Groups.GROUPS.CREATED_BY)
          .values(
              dto.id().value(),
              dto.name().value(),
              dto.description().value(),
              dto.currency().getCurrencyCode(),
              dto.createdBy().value())
          .execute();
    } catch (final DataIntegrityViolationException exception) {
      if (ConstraintViolationUtils.isConstraintViolated(
          exception, Keys.GROUPS__GROUPS_CREATED_BY_FKEY)) {
        final String msg = "User with ID '%s' does not exist".formatted(dto.createdBy().value());
        log.info(msg);
        throw new UserNotFoundException(msg, exception);
      }
      throw exception;
    }
  }

  @Override
  public void updateById(final GroupId id, final UpdateGroupDto dto) {
    final Map<Field<?>, Object> changedFields = new HashMap<>();
    if (dto.name() != null) {
      changedFields.put(Groups.GROUPS.NAME, dto.name().value());
    }
    if (dto.description() != null) {
      changedFields.put(Groups.GROUPS.DESCRIPTION, dto.description().value());
    }
    final int rowsAffected =
        db.update(Groups.GROUPS)
            .set(changedFields)
            .where(Groups.GROUPS.ID.eq(id.value()))
            .execute();
    if (rowsAffected == 0) {
      final String msg = "Group with ID '%s' does not exist".formatted(id.value());
      log.info(msg);
      throw new GroupNotFoundException(msg);
    }
  }

  @Override
  public void deleteById(final GroupId id) {
    final int rowsAffected =
        db.deleteFrom(Groups.GROUPS).where(Groups.GROUPS.ID.eq(id.value())).execute();
    if (rowsAffected == 0) {
      log.warn("Could not delete group with ID: {}", id.value());
    }
  }

  @Transactional(readOnly = true)
  @Override
  public Page<GroupEntity> findByUserId(final UserId userId, final Pageable pageable) {
    final Long total =
        db.selectCount()
            .from(UserGroups.USER_GROUPS)
            .where(UserGroups.USER_GROUPS.USER_ID.eq(userId.value()))
            .fetchOne(0, Long.class);
    final long safeTotal = total != null ? total : 0L;

    final List<SortField<?>> orderFields =
        pageable.getSort().stream()
            .map(
                order -> {
                  Field<?> field = Groups.GROUPS.field(order.getProperty());
                  if (field == null) {
                    throw new IllegalArgumentException(
                        "Unknown sort field: " + order.getProperty());
                  }
                  return order.isAscending() ? field.asc() : field.desc();
                })
            .toList();

    final List<SortField<?>> effectiveOrder =
        orderFields.isEmpty() ? List.of(Groups.GROUPS.CREATED_AT.desc()) : orderFields;

    final List<GroupEntity> data =
        db.select(
                Groups.GROUPS.ID,
                Groups.GROUPS.NAME,
                Groups.GROUPS.DESCRIPTION,
                Groups.GROUPS.CURRENCY_CODE,
                Groups.GROUPS.CREATED_BY)
            .from(UserGroups.USER_GROUPS)
            .join(Groups.GROUPS)
            .on(Groups.GROUPS.ID.eq(UserGroups.USER_GROUPS.GROUP_ID))
            .where(UserGroups.USER_GROUPS.USER_ID.eq(userId.value()))
            .orderBy(effectiveOrder)
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
            Groups.GROUPS.CREATED_BY)
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
        .orderBy(UserGroups.USER_GROUPS.JOINED_AT.asc())
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
  public void addUserToGroup(final UserId userId, final GroupId groupId) {
    try {
      db.insertInto(UserGroups.USER_GROUPS)
          .columns(UserGroups.USER_GROUPS.USER_ID, UserGroups.USER_GROUPS.GROUP_ID)
          .values(userId.value(), groupId.value())
          .execute();
    } catch (final DuplicateKeyException exception) {
      final String msg =
          "User with ID '%s' is already a member of group with ID '%s'"
              .formatted(userId.value(), groupId.value());
      log.info(msg);
      throw new UserAlreadyInGroupException(msg, exception);
    } catch (final DataIntegrityViolationException exception) {
      if (ConstraintViolationUtils.isConstraintViolated(
          exception, Keys.USER_GROUPS__USER_GROUPS_USER_ID_FKEY)) {
        final String msg = "User with ID: %s does not exist".formatted(userId.value());
        log.info(msg);
        throw new UserNotFoundException(msg, exception);
      }
      if (ConstraintViolationUtils.isConstraintViolated(
          exception, Keys.USER_GROUPS__USER_GROUPS_GROUP_ID_FKEY)) {
        final String msg = "Group with ID: %s does not exist".formatted(groupId.value());
        log.info(msg);
        throw new GroupNotFoundException(msg, exception);
      }
      throw exception;
    }
  }
}
