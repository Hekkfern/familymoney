package com.familymoney.familymoney.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mail.sender")
public class MailSenderProperties {

    private String name;
    private String email;
}
