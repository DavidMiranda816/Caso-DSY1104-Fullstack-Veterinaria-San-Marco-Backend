package cl.veterinariasanmarcos.veterinaria_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

class SecurityConfigAuthorizationTest {

    @Test
    void authoritiesComeFromTheCurrentDatabaseRole() {
        UsuarioRepository repository = org.mockito.Mockito.mock(UsuarioRepository.class);
        Usuario usuario = Usuario.builder()
                .id(71L)
                .rol("CLIENTE")
                .activo(true)
                .build();
        when(repository.findById(71L)).thenReturn(Optional.of(usuario));
        Jwt jwt = token("71", List.of("ADMIN"));

        var authentication = new SecurityConfig()
                .jwtAuthenticationConverter(repository)
                .convert(jwt);

        assertEquals(
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")),
                authentication.getAuthorities());
    }

    @Test
    void disabledAccountInvalidatesBearerAuthentication() {
        UsuarioRepository repository = org.mockito.Mockito.mock(UsuarioRepository.class);
        Usuario usuario = Usuario.builder()
                .id(71L)
                .rol("ADMIN")
                .activo(false)
                .build();
        when(repository.findById(71L)).thenReturn(Optional.of(usuario));

        assertThrows(
                InvalidBearerTokenException.class,
                () -> new SecurityConfig()
                        .jwtAuthenticationConverter(repository)
                        .convert(token("71", List.of("ADMIN"))));
    }

    @Test
    void deletedAccountInvalidatesBearerAuthentication() {
        UsuarioRepository repository = org.mockito.Mockito.mock(UsuarioRepository.class);
        when(repository.findById(71L)).thenReturn(Optional.empty());

        assertThrows(
                InvalidBearerTokenException.class,
                () -> new SecurityConfig()
                        .jwtAuthenticationConverter(repository)
                        .convert(token("71", List.of("ADMIN"))));
    }

    @Test
    void expiredJwtIsRejectedByDecoder() {
        SecretKey key = new SecretKeySpec(new byte[32], "HmacSHA256");
        SecurityConfig securityConfig = new SecurityConfig();
        JwtEncoder encoder = securityConfig.jwtEncoder(key);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("test-issuer")
                .subject("71")
                .issuedAt(Instant.now().minusSeconds(120))
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        String expired = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims)).getTokenValue();

        assertThrows(
                JwtException.class,
                () -> securityConfig.jwtDecoder(key, "test-issuer").decode(expired));
    }

    private Jwt token(String subject, List<String> roles) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
    }
}
