package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.GroupName;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class GroupNameConverter implements Converter<String, GroupName> {

  @Override
  public GroupName convert(String source) {
    return GroupName.fromString(source);
  }
}
