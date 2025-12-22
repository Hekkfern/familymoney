package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.GroupName;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateGroupDbo {
  @Nullable @Default private GroupName name = null;
  @Nullable @Default private String description = null;

  public boolean isEmpty() {
    return name == null && description == null;
  }
}
