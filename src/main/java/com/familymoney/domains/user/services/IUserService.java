package com.familymoney.domains.user.services;

import com.familymoney.domains.user.services.data.UpdateUserData;
import com.familymoney.domains.user.services.data.UserData;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
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
