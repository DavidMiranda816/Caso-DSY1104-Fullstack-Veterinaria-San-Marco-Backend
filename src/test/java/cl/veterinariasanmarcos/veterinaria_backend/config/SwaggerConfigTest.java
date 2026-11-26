package cl.veterinariasanmarcos.veterinaria_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class SwaggerConfigTest {

    @Test
    void declaresApiMetadataAndBearerSchemeWithoutMakingEveryOperationPrivate() {
        OpenAPI openAPI = new SwaggerConfig().veterinariaSanMarcosOpenAPI();

        assertEquals("API Veterinaria San Marcos", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        SecurityScheme bearer = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");
        assertEquals(SecurityScheme.Type.HTTP, bearer.getType());
        assertEquals("bearer", bearer.getScheme());
        assertEquals("JWT", bearer.getBearerFormat());
        assertNull(openAPI.getSecurity());
    }
}
