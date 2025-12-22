package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.ShareGroupToken;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class ShareGroupTokenConverter implements Converter<String, ShareGroupToken> {

  @Override
  public ShareGroupToken convert(String source) {
    return ShareGroupToken.fromString(source);
  }
}
