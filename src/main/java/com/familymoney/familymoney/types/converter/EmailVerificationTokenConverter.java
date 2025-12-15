package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.EmailVerificationToken;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class EmailVerificationTokenConverter implements Converter<String, EmailVerificationToken> {

  @Override
  public EmailVerificationToken convert(String source) {
    return EmailVerificationToken.fromString(source);
  }
}
