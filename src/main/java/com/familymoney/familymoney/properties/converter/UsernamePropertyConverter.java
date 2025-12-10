package com.familymoney.familymoney.properties.converter;

import com.familymoney.familymoney.types.Username;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class UsernamePropertyConverter implements Converter<@NonNull String, @NonNull Username> {
  @Override
  public @NonNull Username convert(@com.familymoney.familymoney.validation.Username String source) {
    return Username.of(source);
  }
}
