package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreateTokenFamilyBlacklistDto;
import com.familymoney.domains.auth.repositories.entitites.TokenFamilyBlacklistEntity;
import com.familymoney.domains.auth.repositories.mappers.TokenFamilyBlacklistJooqMapper;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.generated.tables.TokenfamilyBlacklist;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenFamilyBlacklistRepository implements ITokenFamilyBlacklistRepository {

  private final DSLContext db;

  @Override
  public Optional<TokenFamilyBlacklistEntity> create(final CreateTokenFamilyBlacklistDto data) {
    return db.insertInto(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST)
        .columns(
            TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY,
            TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.EXPIRES_AT)
        .values(data.family().value(), OffsetDateTime.ofInstant(data.createdAt(), ZoneOffset.UTC))
        .returning(
            TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY,
            TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.EXPIRES_AT)
        .fetchOptional()
        .map(TokenFamilyBlacklistJooqMapper::toEntity);
  }

  @Override
  public boolean exists(final TokenFamily family) {
    return db.fetchExists(
        db.selectOne()
            .from(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST)
            .where(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY.eq(family.value())));
  }

  @Override
  public boolean deleteByFamily(final TokenFamily family) {
    val rows =
        db.deleteFrom(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST)
            .where(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY.eq(family.value()))
            .execute();
    return rows > 0;
  }
}
