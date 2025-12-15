package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.RefreshToken;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class RefreshTokenConverter implements Converter<String, RefreshToken> {

  @Override
  public RefreshToken convert(String source) {
    return RefreshToken.fromString(source);
  }
}
