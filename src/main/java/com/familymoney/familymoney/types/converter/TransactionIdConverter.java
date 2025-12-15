package com.familymoney.familymoney.types.converter;

import com.familymoney.familymoney.types.TransactionId;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class TransactionIdConverter implements Converter<String, TransactionId> {

  @Override
  public TransactionId convert(String source) {
    return TransactionId.fromString(source);
  }
}
