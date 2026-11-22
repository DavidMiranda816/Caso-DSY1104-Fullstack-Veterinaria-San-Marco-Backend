package cl.veterinariasanmarcos.veterinaria_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigCorsTest {

    @Test
    void allowsOnlyConfiguredFrontendOriginsAndApiMethods() {
        CorsConfiguration configuration =
                new SecurityConfig()
                        .corsConfigurationSource(
                                "http://localhost:5173, https://frontend.example")
                        .getCorsConfiguration(
                                new MockHttpServletRequest(
                                        "OPTIONS", "/api/auth/login"));

        assertEquals(
                List.of("http://localhost:5173", "https://frontend.example"),
                configuration.getAllowedOrigins());
        assertEquals(
                List.of("GET", "POST", "PUT", "PATCH", "OPTIONS"),
                configuration.getAllowedMethods());
        assertNull(configuration.checkOrigin("https://other.example"));
    }
}
