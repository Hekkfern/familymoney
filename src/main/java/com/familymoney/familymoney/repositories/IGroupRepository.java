package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.GroupDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateUserDbo;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.money.CurrencyUnit;

public interface IGroupRepository {

  Optional<GroupDbo> create(
      GroupName name, String description, CurrencyUnit currency, UserId owner);

  boolean updateById(UUID id, UpdateUserDbo data);

  boolean deleteById(UUID id);

  List<GroupDbo> findAllByUserId(UserId userId);

  Optional<GroupDbo> findById(UUID id);
}
