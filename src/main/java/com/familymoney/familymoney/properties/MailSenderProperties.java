package com.familymoney.familymoney.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "mail.sender")
@Validated
@Builder
public record MailSenderProperties(@NotBlank String name, @Email String email) {}
