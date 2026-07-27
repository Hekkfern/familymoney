package com.familymoney.domains.users.repositories;

import com.familymoney.domains.users.repositories.dtos.CreateUserDto;
import com.familymoney.domains.users.repositories.dtos.UpdateUserDto;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.repositories.mappers.UserJooqMapper;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.Users;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

  private final DSLContext db;

  @Override
  public Optional<UserEntity> create(final CreateUserDto data) {

    return db.insertInto(Users.USERS)
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
        .returning(
            Users.USERS.ID,
            Users.USERS.USERNAME,
            Users.USERS.EMAIL,
            Users.USERS.HASHED_PASSWORD,
            Users.USERS.CREATED_AT,
            Users.USERS.UPDATED_AT,
            Users.USERS.IS_EMAIL_VERIFIED,
            Users.USERS.IS_ENABLED)
        .fetchOptional()
        .map(UserJooqMapper::toEntity);
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
  public boolean updateById(final UserId id, final UpdateUserDto data) {
    val rowsAffected =
        db.update(Users.USERS)
            .set(
                Users.USERS.USERNAME,
                DSL.coalesce(
                    DSL.val(data.username() != null ? data.username().value() : null),
                    Users.USERS.USERNAME))
            .set(
                Users.USERS.EMAIL,
                DSL.coalesce(
                    DSL.val(data.email() != null ? data.email().value() : null), Users.USERS.EMAIL))
            .set(
                Users.USERS.HASHED_PASSWORD,
                DSL.coalesce(
                    DSL.val(data.hashedPassword() != null ? data.hashedPassword() : null),
                    Users.USERS.HASHED_PASSWORD))
            .set(
                Users.USERS.IS_EMAIL_VERIFIED,
                DSL.coalesce(DSL.val(data.isEmailVerified()), Users.USERS.IS_EMAIL_VERIFIED))
            .set(
                Users.USERS.IS_ENABLED,
                DSL.coalesce(DSL.val(data.isEnabled()), Users.USERS.IS_ENABLED))
            .where(Users.USERS.ID.eq(id.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(final UserId id) {
    val rowsAffected = db.deleteFrom(Users.USERS).where(Users.USERS.ID.eq(id.value())).execute();
    return rowsAffected > 0;
  }

  @Override
  public Page<UserEntity> getAll(final Pageable pageable) {
    val totalField = DSL.count().over().as("total_count");

    val orderFields =
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

    val effectiveOrder =
        orderFields.isEmpty() ? List.of(Users.USERS.CREATED_AT.desc()) : orderFields;

    val records =
        db.select(
                Users.USERS.ID,
                Users.USERS.USERNAME,
                Users.USERS.EMAIL,
                Users.USERS.HASHED_PASSWORD,
                Users.USERS.CREATED_AT,
                Users.USERS.UPDATED_AT,
                Users.USERS.IS_EMAIL_VERIFIED,
                Users.USERS.IS_ENABLED,
                totalField)
            .from(Users.USERS)
            .orderBy(effectiveOrder)
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch();

    val total = records.isEmpty() ? 0L : records.getFirst().get("total_count", Long.class);
    val data = records.map(UserJooqMapper::toEntity);

    return new PageImpl<>(data, pageable, total);
  }
}
