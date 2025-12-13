package com.familymoney.familymoney.types.converter;

import jakarta.validation.constraints.Email;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class EmailConverter
    implements Converter<String, com.familymoney.familymoney.types.Email> {
  @Override
  public com.familymoney.familymoney.types.Email convert(@Email String source) {
    return com.familymoney.familymoney.types.Email.fromString(source);
  }
}
