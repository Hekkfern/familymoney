package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreateTokenFamilyBlacklistDto;
import com.familymoney.domains.auth.repositories.entitites.TokenFamilyBlacklistEntity;
import com.familymoney.domains.auth.types.TokenFamily;

import java.util.Optional;

public interface ITokenFamilyBlacklistRepository {

   Optional<TokenFamilyBlacklistEntity> create(CreateTokenFamilyBlacklistDto data);

   boolean exists(TokenFamily family);

   boolean deleteByFamily(TokenFamily family);
}
