package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dbos.CreateTokenFamilyBlacklistDbo;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.generated.tables.TokenfamilyBlacklist;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DefaultTokenFamilyBlacklistRepository implements TokenFamilyBlacklistRepository {

  private final DSLContext db;

  @Override
  public void create(final CreateTokenFamilyBlacklistDbo data) {
    db.insertInto(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST)
        .columns(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY)
        .values(data.family().value())
        .onConflictDoNothing()
        .execute();
  }

  @Override
  public boolean exists(final TokenFamily family) {
    return db.fetchExists(
        db.selectOne()
            .from(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST)
            .where(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY.eq(family.value())));
  }

  @Override
  public void deleteByFamily(final TokenFamily family) {
    db.deleteFrom(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST)
        .where(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY.eq(family.value()))
        .execute();
  }
}
