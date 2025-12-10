package com.familymoney.familymoney.properties.converter;

import jakarta.validation.constraints.Email;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class EmailPropertyConverter
    implements Converter<@NonNull String, com.familymoney.familymoney.types.Email> {
  @Override
  public com.familymoney.familymoney.types.Email convert(@Email String source) {
    return com.familymoney.familymoney.types.Email.of(source);
  }
}
