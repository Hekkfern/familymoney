package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.Password;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class PasswordConverter implements Converter<String, Password> {

  @Override
  public Password convert(String source) {
    return Password.fromString(source);
  }
}
