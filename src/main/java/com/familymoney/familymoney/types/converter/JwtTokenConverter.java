package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.JwtToken;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class JwtTokenConverter implements Converter<String, JwtToken> {

  @Override
  public JwtToken convert(String source) {
    return JwtToken.fromString(source);
  }
}
