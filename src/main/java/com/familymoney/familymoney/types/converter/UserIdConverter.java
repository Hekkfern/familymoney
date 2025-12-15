package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.UserId;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class UserIdConverter implements Converter<String, UserId> {

  @Override
  public UserId convert(String source) {
    return UserId.fromString(source);
  }
}
