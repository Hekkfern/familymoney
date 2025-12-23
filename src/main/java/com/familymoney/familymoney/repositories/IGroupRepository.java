package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.GroupDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateGroupDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.util.List;
import java.util.Optional;
import javax.money.CurrencyUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IGroupRepository {

  Optional<GroupDbo> create(
      GroupName name, String description, CurrencyUnit currency, UserId owner);

  boolean updateById(GroupId id, UpdateGroupDbo data);

  boolean deleteById(GroupId id);

  Page<GroupDbo> findAllByUserId(UserId userId, Pageable pageable);

  Optional<GroupDbo> findById(GroupId id);

  List<UserId> findUserIdsByGroupId(GroupId id);

  boolean isUserInGroup(UserId userId, GroupId groupId);
}
