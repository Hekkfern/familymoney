package com.familymoney.familymoney.services.data;

import com.familymoney.familymoney.types.GroupName;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateGroupData {
  @Nullable @Default private GroupName name = null;
  @Nullable @Default private String description = null;

  public boolean isEmpty() {
    return name == null && description == null;
  }
}
