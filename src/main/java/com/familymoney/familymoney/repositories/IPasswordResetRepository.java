package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.PasswordResetDbo;
import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.Optional;
import org.springframework.lang.NonNull;

public interface IPasswordResetRepository {

    @NonNull
    Optional<PasswordResetDbo> create(@NonNull UserId userId, @NonNull PasswordResetToken token,
            @NonNull Instant expiresAt);

    @NonNull
    Optional<PasswordResetDbo> findByToken(@NonNull PasswordResetToken token);

    void deleteByUserId(@NonNull UserId userId);
}
