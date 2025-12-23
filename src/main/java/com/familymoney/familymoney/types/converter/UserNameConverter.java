package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.UserName;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class UserNameConverter implements Converter<String, UserName> {

  @Override
  public UserName convert(String source) {
    return UserName.fromString(source);
  }
}
