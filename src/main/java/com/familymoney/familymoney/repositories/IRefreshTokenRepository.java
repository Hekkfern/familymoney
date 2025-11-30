package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import org.springframework.lang.NonNull;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository {

    @NonNull
    Optional<RefreshTokenDbo> create(@NonNull UserId userId, @NonNull RefreshToken token, UUID family);

    @NonNull
    Optional<RefreshTokenDbo> findByToken(@NonNull RefreshToken token);

    void markTokenAsUsed(@NonNull RefreshToken token);

    void invalidateByFamily(UUID family);

    void invalidateByUserId(@NonNull UserId userId);

    void deleteOlderThan(@NonNull Duration cutoff);
}
