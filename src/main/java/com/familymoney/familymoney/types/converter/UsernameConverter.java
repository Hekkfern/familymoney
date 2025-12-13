package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.Username;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class UsernameConverter implements Converter<String, Username> {
  @Override
  public Username convert(@com.familymoney.familymoney.validation.Username String source) {
    return Username.of(source);
  }
}
