package com.familymoney.familymoney.properties.converter;

import com.familymoney.familymoney.types.Password;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class PasswordPropertyConverter implements Converter<@NonNull String, @NonNull Password> {
  @Override
  public @NonNull Password convert(@com.familymoney.familymoney.validation.Password String source) {
    return Password.of(source);
  }
}
