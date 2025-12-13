package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.PasswordResetToken;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class PasswordResetTokenConverter implements Converter<String, PasswordResetToken> {
  @Override
  public PasswordResetToken convert(String source) {
    return PasswordResetToken.fromString(source);
  }
}
