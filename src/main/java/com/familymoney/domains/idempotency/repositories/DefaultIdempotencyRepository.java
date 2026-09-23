package com.familymoney.domains.idempotency.repositories;

import com.familymoney.domains.idempotency.repositories.dtos.CachedResponseDto;
import com.familymoney.domains.idempotency.repositories.dtos.ReserveIdempotencyKeyDto;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyEntry;
import com.familymoney.domains.idempotency.repositories.mappers.IdempotencyEntryJooqMapper;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.IdempotencyKeys;
import com.familymoney.properties.IdempotencyProperties;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DefaultIdempotencyRepository implements IdempotencyRepository {

  private final DSLContext db;
  private final IdempotencyProperties properties;
  private final Clock clock;

  @Override
  public boolean reserve(final ReserveIdempotencyKeyDto dto) {
    final OffsetDateTime now = OffsetDateTime.now(clock);
    final OffsetDateTime expiresAt = now.plus(properties.keyDuration());
    final int insertedRows =
        db.insertInto(IdempotencyKeys.IDEMPOTENCY_KEYS)
            .columns(
                IdempotencyKeys.IDEMPOTENCY_KEYS.USER_ID,
                IdempotencyKeys.IDEMPOTENCY_KEYS.KEY,
                IdempotencyKeys.IDEMPOTENCY_KEYS.REQUEST_HASH,
                IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT)
            .values(dto.userId().value(), dto.key().value(), dto.requestHash(), expiresAt)
            .onConflict(
                IdempotencyKeys.IDEMPOTENCY_KEYS.USER_ID, IdempotencyKeys.IDEMPOTENCY_KEYS.KEY)
            .doUpdate()
            .set(IdempotencyKeys.IDEMPOTENCY_KEYS.REQUEST_HASH, dto.requestHash())
            .set(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_STATUS, (Integer) null)
            .set(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_BODY, (JSONB) null)
            .set(IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT, expiresAt)
            .where(IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT.lt(now))
            .execute();
    return insertedRows > 0;
  }

  @Override
  public void complete(final UserId userId, final IdempotencyKey key, final CachedResponseDto dto) {
    db.update(IdempotencyKeys.IDEMPOTENCY_KEYS)
        .set(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_STATUS, dto.httpStatus())
        .set(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_BODY, JSONB.valueOf(dto.body()))
        .where(
            IdempotencyKeys.IDEMPOTENCY_KEYS
                .USER_ID
                .eq(userId.value())
                .and(IdempotencyKeys.IDEMPOTENCY_KEYS.KEY.eq(key.value()))
                .and(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_STATUS.isNull())
                .and(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_BODY.isNull()))
        .execute();
  }

  @Override
  public Optional<IdempotencyEntry> findByKey(final UserId userId, final IdempotencyKey key) {
    return db.select(
            IdempotencyKeys.IDEMPOTENCY_KEYS.USER_ID,
            IdempotencyKeys.IDEMPOTENCY_KEYS.KEY,
            IdempotencyKeys.IDEMPOTENCY_KEYS.REQUEST_HASH,
            IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_STATUS,
            IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_BODY,
            IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT)
        .from(IdempotencyKeys.IDEMPOTENCY_KEYS)
        .where(
            IdempotencyKeys.IDEMPOTENCY_KEYS
                .USER_ID
                .eq(userId.value())
                .and(IdempotencyKeys.IDEMPOTENCY_KEYS.KEY.eq(key.value()))
                .and(IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT.gt(OffsetDateTime.now(clock))))
        .fetchOptional()
        .map(IdempotencyEntryJooqMapper::toEntity);
  }

  @Override
  public int deleteExpired(final int batchSize) {
    final OffsetDateTime now = OffsetDateTime.now(clock);
    return db.deleteFrom(IdempotencyKeys.IDEMPOTENCY_KEYS)
        .where(
            DSL.row(IdempotencyKeys.IDEMPOTENCY_KEYS.USER_ID, IdempotencyKeys.IDEMPOTENCY_KEYS.KEY)
                .in(
                    db.select(
                            IdempotencyKeys.IDEMPOTENCY_KEYS.USER_ID,
                            IdempotencyKeys.IDEMPOTENCY_KEYS.KEY)
                        .from(IdempotencyKeys.IDEMPOTENCY_KEYS)
                        .where(IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT.lt(now))
                        .orderBy(IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT.asc())
                        .limit(batchSize)))
        .execute();
  }
}
