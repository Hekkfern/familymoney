package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dbos.CreateTokenFamilyBlacklistDbo;
import com.familymoney.domains.auth.types.TokenFamily;

public interface TokenFamilyBlacklistRepository {

  void create(CreateTokenFamilyBlacklistDbo data);

  boolean exists(TokenFamily family);

  void deleteByFamily(TokenFamily family);
}
