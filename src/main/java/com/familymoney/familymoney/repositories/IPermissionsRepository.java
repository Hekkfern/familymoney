package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.types.UserId;
import java.util.List;
import org.springframework.lang.NonNull;

public interface IPermissionsRepository {

  @NonNull
  List<String> getPermissionsByUserId(@NonNull UserId userId);

  void setPermissionsForUserId(@NonNull UserId userId, @NonNull List<String> permissions);

  void deletePermissionsByUserId(@NonNull UserId userId, @NonNull List<String> permissions);

  void setRoleForUserId(@NonNull UserId userId, @NonNull String role);
}
