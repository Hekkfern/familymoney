package com.familymoney.services;

import com.familymoney.services.data.UserData;
import com.familymoney.services.data.UpdateUserData;
import com.familymoney.types.*;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

  Optional<UserData> getUserData(UserId userId);

  void deleteUser(UserId userId);

  void updateUserInfo(UserId userId, UpdateUserData data);

  Page<UserData> getUsers(Pageable pageable);

  void enableUser(UserId userId, boolean enabled);

  void setUserRole(UserId userId, Role role);

  Optional<Role> getUserRole(UserId userId);

  void createAdminUser(UserName username, Email email, Password password);
}
