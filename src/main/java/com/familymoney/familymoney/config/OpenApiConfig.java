package com.familymoney.familymoney.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
    info =
        @Info(
            title = "\"Family Money\" backend",
            version = "1.0",
            description = "Backend API for \"Family Money\" application",
            contact =
                @Contact(name = "Hector Fernandez", email = "hector.fernandez.carmona@gmail.com")),
    security = {@SecurityRequirement(name = "bearerAuth")})
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT authentication",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {}
