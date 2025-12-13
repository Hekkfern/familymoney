package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.GroupId;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class GroupIdConverter implements Converter<String, GroupId> {
  @Override
  public GroupId convert(String source) {
    return GroupId.fromString(source);
  }
}
