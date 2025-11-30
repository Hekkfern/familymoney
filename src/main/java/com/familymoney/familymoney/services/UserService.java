package com.familymoney.familymoney.services;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.services.data.UserData;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.Password;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    @Override
    @NonNull
    public UserData getMyUserData(@NonNull UserId userId) {
        val userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new DatabaseExecutionException("User not found with id: $userId");
        }
        val user = userOpt.get();
        return new UserData(user.username(), user.email(), user.createdAt());
    }

    @Override
    public void deleteMyUser(@NonNull UserId userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public void updateMyUser(@NonNull UserId userId, @NonNull Optional<Username> username,
            @NonNull Optional<Email> email, @NonNull Optional<Password> password) {

    }
}
