package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.Email;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class EmailConverter implements Converter<String, Email> {

  @Override
  public Email convert(String source) {
    return Email.fromString(source);
  }
}
