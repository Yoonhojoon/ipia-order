package com.ipia.order.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "IPIA Order API", version = "v1"),
    security = {
        @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_BEARER)
    }
)
@SecurityScheme(
    name = OpenApiConfig.SECURITY_SCHEME_BEARER,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    public static final String SECURITY_SCHEME_BEARER = "BearerAuth";
}


