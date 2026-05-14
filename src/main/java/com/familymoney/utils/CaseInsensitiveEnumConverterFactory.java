package com.familymoney.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum> {

  @Override
  public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
    return new CaseInsensitiveEnumConverter<>(targetType);
  }

  private static class CaseInsensitiveEnumConverter<T extends Enum>
      implements Converter<String, T> {

    private final Class<T> enumType;

    CaseInsensitiveEnumConverter(Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    public T convert(String source) {
      for (T constant : enumType.getEnumConstants()) {
        if (constant.name().equalsIgnoreCase(source)) {
          return constant;
        }
      }
      throw new IllegalArgumentException(
          "Invalid enum value '%s' for enum %s".formatted(source, enumType.getSimpleName()));
    }
  }
}
