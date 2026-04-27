package com.familymoney.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {

  @Bean
  public ClassLoaderTemplateResolver htmlTemplateResolver() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

    resolver.setPrefix("templates/mail/"); // Location of thymeleaf template
    resolver.setCacheable(false); // Turning off cache to facilitate template changes
    resolver.setSuffix(".html"); // Template file extension
    resolver.setTemplateMode(TemplateMode.HTML); // Template Type
    resolver.setCharacterEncoding("UTF-8");

    return resolver;
  }

  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(htmlTemplateResolver());
    return engine;
  }
}
