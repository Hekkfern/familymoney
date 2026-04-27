package com.familymoney.repositories.dtos;

import com.familymoney.types.GroupName;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@Builder
public class UpdateGroupDto {
  @Nullable @Default private GroupName name = null;
  @Nullable @Default private String description = null;

  public boolean isEmpty() {
    return name == null && description == null;
  }
}
