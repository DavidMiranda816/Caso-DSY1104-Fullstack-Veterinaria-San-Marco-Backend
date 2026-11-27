package cl.veterinariasanmarcos.veterinaria_backend.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.controller.AuthController;
import cl.veterinariasanmarcos.veterinaria_backend.controller.UsuarioController;
import cl.veterinariasanmarcos.veterinaria_backend.dto.AdministradorRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioAltaRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;
import cl.veterinariasanmarcos.veterinaria_backend.services.AuthService;
import cl.veterinariasanmarcos.veterinaria_backend.services.UsuarioService;

@WebMvcTest(controllers = {AuthController.class, UsuarioController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=test-issuer",
        "app.jwt.duration-seconds=3600",
        "app.cors.allowed-origins=http://localhost"
})
class AdminAuthorizationHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AuthService authService;

    @Test
    void activeAdminCanReadUserDetails() throws Exception {
        when(usuarioRepository.findById(7L))
                .thenReturn(Optional.of(usuario(7L, "ADMIN", true)));
        when(usuarioService.obtenerPorId(2L))
                .thenReturn(UsuarioResponse.builder()
                        .id(2L)
                        .nombre("Cliente")
                        .correo("cliente@example.com")
                        .rol("CLIENTE")
                        .activo(true)
                        .build());

        mockMvc.perform(get("/api/usuarios/2")
                        .header("Authorization", "Bearer " + token("7", "CLIENTE", 60)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("cliente@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void missingUserReturnsNotFound() throws Exception {
        when(usuarioRepository.findById(7L))
                .thenReturn(Optional.of(usuario(7L, "ADMIN", true)));
        when(usuarioService.obtenerPorId(404L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mockMvc.perform(get("/api/usuarios/404")
                        .header("Authorization", "Bearer " + token("7", "ADMIN", 60)))
                .andExpect(status().isNotFound());
    }

    @Test
    void activeAdminCanCreateAnotherAdmin() throws Exception {
        when(usuarioRepository.findById(7L))
                .thenReturn(Optional.of(usuario(7L, "ADMIN", true)));
        when(authService.registrarAdministrador(any(AdministradorRequest.class)))
                .thenReturn(UsuarioResponse.builder()
                        .id(12L)
                        .nombre("Nuevo admin")
                        .correo("nuevo@example.com")
                        .rol("ADMIN")
                        .activo(true)
                        .build());

        mockMvc.perform(post("/api/auth/admin/registro")
                        .header("Authorization", "Bearer " + token("7", "CLIENTE", 60))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Nuevo admin",
                                  "correo": "nuevo@example.com",
                                  "password": "contrasena-segura"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void activeAdminCreatesKaijuUserWithRequestedRole() throws Exception {
        when(usuarioRepository.findById(7L))
                .thenReturn(Optional.of(usuario(7L, "ADMIN", true)));
        when(authService.registrarUsuarioKaiju(any(UsuarioAltaRequest.class)))
                .thenReturn(UsuarioResponse.builder().id(12L).rol("VENDEDOR").build());

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + token("7", "ADMIN", 60))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(altaKaiju("VENDEDOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"));

        var solicitud = org.mockito.ArgumentCaptor.forClass(UsuarioAltaRequest.class);
        org.mockito.Mockito.verify(authService).registrarUsuarioKaiju(solicitud.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                UsuarioAltaRequest.RolKaiju.VENDEDOR, solicitud.getValue().getRol());
    }

    @Test
    void nonAdminCannotCreateKaijuUsers() throws Exception {
        when(usuarioRepository.findById(8L))
                .thenReturn(Optional.of(usuario(8L, "ENCARGADO", true)));

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + token("8", "ENCARGADO", 60))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(altaKaiju("VENDEDOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService);
    }

    @Test
    void kaijuCreationRejectsVeterinaryRole() throws Exception {
        when(usuarioRepository.findById(7L))
                .thenReturn(Optional.of(usuario(7L, "ADMIN", true)));

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + token("7", "ADMIN", 60))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(altaKaiju("CLIENTE")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    private String altaKaiju(String rol) {
        return """
                {
                  "nombre": "Usuario Kaiju",
                  "correo": "nuevo@example.com",
                  "password": "contrasena-segura",
                  "rol": "%s"
                }
                """.formatted(rol);
    }

    @Test
    void anyActiveRoleCanRevalidateItsSession() throws Exception {
        when(usuarioRepository.findById(11L))
                .thenReturn(Optional.of(usuario(11L, "CLIENTE", true)));
        when(authService.obtenerUsuarioActual("11"))
                .thenReturn(UsuarioResponse.builder()
                        .id(11L)
                        .nombre("Cliente")
                        .correo("cliente@example.com")
                        .rol("CLIENTE")
                        .activo(true)
                        .build());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token("11", "CLIENTE", 60)))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedNonAdminIsDeniedFromUserManagement() throws Exception {
        when(usuarioRepository.findById(8L))
                .thenReturn(Optional.of(usuario(8L, "CLIENTE", true)));

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token("8", "ADMIN", 60)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void disabledAccountReceivesUnauthorized() throws Exception {
        when(usuarioRepository.findById(9L))
                .thenReturn(Optional.of(usuario(9L, "ADMIN", false)));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token("9", "ADMIN", 60)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenReceivesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token("10", "ADMIN", -60)))
                .andExpect(status().isUnauthorized());
    }

    private Usuario usuario(Long id, String rol, boolean activo) {
        return Usuario.builder()
                .id(id)
                .rol(rol)
                .activo(activo)
                .build();
    }

    private String token(String subject, String role, long lifetimeSeconds) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("test-issuer")
                .subject(subject)
                .issuedAt(lifetimeSeconds < 0
                        ? now.plusSeconds(lifetimeSeconds - 1)
                        : now)
                .expiresAt(now.plusSeconds(lifetimeSeconds))
                .claim("roles", List.of(role))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims)).getTokenValue();
    }
}
