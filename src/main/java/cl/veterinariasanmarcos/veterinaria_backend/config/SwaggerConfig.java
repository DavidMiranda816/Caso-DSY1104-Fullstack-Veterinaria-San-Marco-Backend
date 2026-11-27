package cl.veterinariasanmarcos.veterinaria_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI veterinariaSanMarcosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Veterinaria San Marcos")
                        .version("1.0.0")
                        .description(
                                "API REST para autenticación, administración de cuentas y "
                                        + "catálogo de Veterinaria San Marcos. Las operaciones "
                                        + "de negocio requieren un JWT Bearer vigente; la "
                                        + "autorización se resuelve con el rol y estado actuales "
                                        + "de la cuenta."))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
