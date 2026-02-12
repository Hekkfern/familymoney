package com.familymoney.familymoney.repositories.impl;

import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.UpdateUserDbo;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.repositories.mappers.UserJooqMapper;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

  private final DSLContext db;

  @Override
  public Optional<UserDbo> create(
      final UserName username, final Email email, final String passwordHash) {

    return db.insertInto(Users.USERS)
        .columns(Users.USERS.USERNAME, Users.USERS.EMAIL, Users.USERS.HASHED_PASSWORD)
        .values(username.value(), email.value(), passwordHash)
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
        .map(UserJooqMapper::toDbo);
  }

  @Override
  public Optional<UserDbo> findById(final UserId id) {
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
        .map(UserJooqMapper::toDbo);
  }

  @Override
  public Optional<UserDbo> findByEmail(final Email email) {
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
        .map(UserJooqMapper::toDbo);
  }

  @Override
  public Optional<UserDbo> findByUsername(final UserName username) {
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
        .map(UserJooqMapper::toDbo);
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
  public boolean updateById(final UserId id, final UpdateUserDbo data) {
    val rowsAffected =
        db.update(Users.USERS)
            .set(
                Users.USERS.USERNAME,
                DSL.coalesce(
                    DSL.val(data.getUsername() != null ? data.getUsername().value() : null),
                    Users.USERS.USERNAME))
            .set(
                Users.USERS.EMAIL,
                DSL.coalesce(
                    DSL.val(data.getEmail() != null ? data.getEmail().value() : null),
                    Users.USERS.EMAIL))
            .set(
                Users.USERS.HASHED_PASSWORD,
                DSL.coalesce(
                    DSL.val(data.getHashedPassword() != null ? data.getHashedPassword() : null),
                    Users.USERS.HASHED_PASSWORD))
            .set(
                Users.USERS.IS_EMAIL_VERIFIED,
                DSL.coalesce(DSL.val(data.getIsEmailVerified()), Users.USERS.IS_EMAIL_VERIFIED))
            .set(
                Users.USERS.IS_ENABLED,
                DSL.coalesce(DSL.val(data.getIsEnabled()), Users.USERS.IS_ENABLED))
            .where(Users.USERS.ID.eq(id.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(final UserId id) {
    return db.deleteFrom(Users.USERS).where(Users.USERS.ID.eq(id.value())).execute() > 0;
  }

  @Transactional
  @Override
  public Page<UserDbo> findAll(final Pageable pageable) {
    val total = db.selectCount().from(Users.USERS).fetchOne(0, Long.class);
    val safeTotal = total != null ? total : 0L;
    val data =
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
            .orderBy(Users.USERS.CREATED_AT.desc())
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch()
            .map(UserJooqMapper::toDbo);
    return new PageImpl<>(data, pageable, safeTotal);
  }
}
