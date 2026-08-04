package com.familymoney.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer trimmingStringDeserializerCustomizer() {
        return builder -> builder.addModule(
                new SimpleModule().addDeserializer(String.class, new TrimmingStringDeserializer()));
    }

    private static class TrimmingStringDeserializer extends StringDeserializer {

        @Override
        public @Nullable String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String value = super.deserialize(p, ctxt);
            return value == null ? null : value.trim();
        }
    }
}
