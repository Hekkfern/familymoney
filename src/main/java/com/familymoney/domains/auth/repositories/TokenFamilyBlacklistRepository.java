package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dbos.CreateTokenFamilyBlacklistDbo;
import com.familymoney.domains.auth.repositories.entitites.TokenFamilyBlacklistEntity;
import com.familymoney.domains.auth.types.TokenFamily;
import java.util.Optional;

public interface TokenFamilyBlacklistRepository {

  Optional<TokenFamilyBlacklistEntity> create(CreateTokenFamilyBlacklistDbo data);

  boolean exists(TokenFamily family);

  boolean deleteByFamily(TokenFamily family);
}
