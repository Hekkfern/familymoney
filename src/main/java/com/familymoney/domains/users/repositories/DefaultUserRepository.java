package com.familymoney.domains.users.repositories;

import com.familymoney.domains.users.repositories.dbos.CreateUserDbo;
import com.familymoney.domains.users.repositories.dbos.UpdateUserDbo;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.repositories.mappers.UserJooqMapper;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.Users;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DefaultUserRepository implements UserRepository {

  private final DSLContext db;

  @Override
  public void create(final CreateUserDbo data) {
    db.insertInto(Users.USERS)
        .columns(
            Users.USERS.ID,
            Users.USERS.USERNAME,
            Users.USERS.EMAIL,
            Users.USERS.HASHED_PASSWORD,
            Users.USERS.IS_EMAIL_VERIFIED,
            Users.USERS.IS_ENABLED)
        .values(
            data.id().value(),
            data.username().value(),
            data.email().value(),
            data.passwordHash(),
            data.isEmailVerified(),
            data.isEnabled())
        .execute();
  }

  @Override
  public Optional<UserEntity> findById(final UserId id) {
    return db.select(
            Users.USERS.ID,
            Users.USERS.USERNAME,
            Users.USERS.EMAIL,
            Users.USERS.HASHED_PASSWORD,
            Users.USERS.CREATED_AT,
            Users.USERS.UPDATED_AT,
            Users.USERS.IS_EMAIL_VERIFIED,
            Users.USERS.IS_ENABLED)
        .from(Users.USERS)
        .where(Users.USERS.ID.eq(id.value()))
        .fetchOptional()
        .map(UserJooqMapper::toEntity);
  }

  @Override
  public Optional<UserEntity> findByEmail(final Email email) {
    return db.select(
            Users.USERS.ID,
            Users.USERS.USERNAME,
            Users.USERS.EMAIL,
            Users.USERS.HASHED_PASSWORD,
            Users.USERS.CREATED_AT,
            Users.USERS.UPDATED_AT,
            Users.USERS.IS_EMAIL_VERIFIED,
            Users.USERS.IS_ENABLED)
        .from(Users.USERS)
        .where(Users.USERS.EMAIL.eq(email.value()))
        .fetchOptional()
        .map(UserJooqMapper::toEntity);
  }

  @Override
  public Optional<UserEntity> findByUsername(final UserName username) {
    return db.select(
            Users.USERS.ID,
            Users.USERS.USERNAME,
            Users.USERS.EMAIL,
            Users.USERS.HASHED_PASSWORD,
            Users.USERS.CREATED_AT,
            Users.USERS.UPDATED_AT,
            Users.USERS.IS_EMAIL_VERIFIED,
            Users.USERS.IS_ENABLED)
        .from(Users.USERS)
        .where(Users.USERS.USERNAME.eq(username.value()))
        .fetchOptional()
        .map(UserJooqMapper::toEntity);
  }

  @Override
  public boolean existsByEmailOrUsername(final Email email, final UserName username) {
    return db.fetchExists(
        db.selectOne()
            .from(Users.USERS)
            .where(
                Users.USERS.EMAIL.eq(email.value()).or(Users.USERS.USERNAME.eq(username.value()))));
  }

  @Override
  public boolean existsById(UserId id) {
    return db.fetchExists(db.selectOne().from(Users.USERS).where(Users.USERS.ID.eq(id.value())));
  }

  @Override
  public void updateById(final UserId id, final UpdateUserDbo data) {
    final Map<Field<?>, Object> changedFields = new HashMap<>();
    if (data.username() != null) {
      changedFields.put(Users.USERS.USERNAME, data.username().value());
    }
    if (data.email() != null) {
      changedFields.put(Users.USERS.EMAIL, data.email().value());
    }
    if (data.hashedPassword() != null) {
      changedFields.put(Users.USERS.HASHED_PASSWORD, data.hashedPassword());
    }
    if (data.isEmailVerified() != null) {
      changedFields.put(Users.USERS.IS_EMAIL_VERIFIED, data.isEmailVerified());
    }
    if (data.isEnabled() != null) {
      changedFields.put(Users.USERS.IS_ENABLED, data.isEnabled());
    }
    db.update(Users.USERS).set(changedFields).where(Users.USERS.ID.eq(id.value())).execute();
  }

  @Override
  public void deleteById(final UserId id) {
    db.deleteFrom(Users.USERS).where(Users.USERS.ID.eq(id.value())).execute();
  }

  @Override
  public Page<UserEntity> getAll(final Pageable pageable) {
    final Long total = db.selectCount().from(Users.USERS).fetchOne(0, Long.class);
    final long safeTotal = total != null ? total : 0L;

    final List<SortField<?>> orderFields =
        pageable.getSort().stream()
            .map(
                order -> {
                  Field<?> field = Users.USERS.field(order.getProperty());
                  if (field == null) {
                    throw new IllegalArgumentException(
                        "Unknown sort field: " + order.getProperty());
                  }
                  return order.isAscending() ? field.asc() : field.desc();
                })
            .toList();

    final List<SortField<?>> effectiveOrder =
        orderFields.isEmpty() ? List.of(Users.USERS.CREATED_AT.desc()) : orderFields;

    final List<UserEntity> data =
        db.select(
                Users.USERS.ID,
                Users.USERS.USERNAME,
                Users.USERS.EMAIL,
                Users.USERS.HASHED_PASSWORD,
                Users.USERS.CREATED_AT,
                Users.USERS.UPDATED_AT,
                Users.USERS.IS_EMAIL_VERIFIED,
                Users.USERS.IS_ENABLED)
            .from(Users.USERS)
            .orderBy(effectiveOrder)
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch()
            .map(UserJooqMapper::toEntity);

    return new PageImpl<>(data, pageable, safeTotal);
  }
}
