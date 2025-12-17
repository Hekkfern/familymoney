package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.services.data.UpdateUserData;
import com.familymoney.familymoney.types.*;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

  Optional<GetUserData> getUserData(UserId userId);

  void deleteUser(UserId userId);

  void updateUserInfo(UserId userId, UpdateUserData data);

  Page<GetUserData> getUsers(Pageable pageable);

  void enableUser(UserId userId, boolean enabled);

  void setUserRole(UserId userId, Role role);

  Optional<Role> getUserRole(UserId userId);

  void createAdminUser(Username username, Email email, Password password);
}
