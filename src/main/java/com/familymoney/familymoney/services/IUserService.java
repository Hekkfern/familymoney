package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.UserData;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import org.springframework.lang.NonNull;
import java.util.Optional;

public interface IUserService {

    @NonNull
    UserData getMyUserData(@NonNull UserId userId);

    void deleteMyUser(@NonNull UserId userId);

    void updateMyUser(@NonNull UserId userId, @NonNull Optional<Username> username, @NonNull Optional<Email> email,
            @NonNull Optional<Password> password);
}
