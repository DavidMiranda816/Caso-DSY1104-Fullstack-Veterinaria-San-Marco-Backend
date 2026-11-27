package cl.veterinariasanmarcos.veterinaria_backend.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import org.springframework.http.HttpStatus;

import cl.veterinariasanmarcos.veterinaria_backend.controller.SolicitudController;
import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudResponse;
import cl.veterinariasanmarcos.veterinaria_backend.exception.ApiExceptionHandler;
import cl.veterinariasanmarcos.veterinaria_backend.model.SolicitudEstado;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;
import cl.veterinariasanmarcos.veterinaria_backend.services.SolicitudService;

@WebMvcTest(SolicitudController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
@TestPropertySource(properties = {
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=test-issuer",
        "app.jwt.duration-seconds=3600",
        "app.cors.allowed-origins=http://localhost"
})
class SolicitudAuthorizationHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private SolicitudService solicitudService;

    @Test
    void receptionistCanListReadAndChangeAppointmentState() throws Exception {
        when(usuarioRepository.findById(12L))
                .thenReturn(Optional.of(usuario(12L, "RECEPCIONISTA")));
        when(solicitudService.listar(12L, false)).thenReturn(List.of(response(15L)));
        when(solicitudService.obtenerPorId(15L, 12L, false)).thenReturn(response(15L));
        when(solicitudService.cambiarEstado(
                eq(15L), any()))
                .thenReturn(response(15L));

        String authorization = "Bearer " + token("12");
        mockMvc.perform(get("/api/v1/solicitudes").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("CITA"));
        mockMvc.perform(get("/api/v1/solicitudes/15")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15));
        mockMvc.perform(patch("/api/v1/solicitudes/15/estado")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"CONFIRMADA"}
                                """))
                .andExpect(status().isOk());

        verify(solicitudService).cambiarEstado(
                eq(15L), any());
    }

    @Test
    void receptionistCannotDeleteAppointmentsOrManageUsers() throws Exception {
        when(usuarioRepository.findById(12L))
                .thenReturn(Optional.of(usuario(12L, "RECEPCIONISTA")));
        String authorization = "Bearer " + token("12");

        mockMvc.perform(delete("/api/v1/solicitudes/15")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());

        verifyNoInteractions(solicitudService);
    }

    @Test
    void invalidBearerTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/solicitudes")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTransitionReturnsConflictWithFrontendMessage() throws Exception {
        when(usuarioRepository.findById(12L))
                .thenReturn(Optional.of(usuario(12L, "RECEPCIONISTA")));
        when(solicitudService.cambiarEstado(
                eq(15L), any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se puede cambiar una cita de PENDIENTE a COMPLETADA"));

        mockMvc.perform(patch("/api/v1/solicitudes/15/estado")
                        .header("Authorization", "Bearer " + token("12"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"COMPLETADA"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("No se puede cambiar una cita de PENDIENTE a COMPLETADA"));
    }

    @Test
    void nonexistentFolioReturnsNotFound() throws Exception {
        when(usuarioRepository.findById(12L))
                .thenReturn(Optional.of(usuario(12L, "RECEPCIONISTA")));
        when(solicitudService.obtenerPorId(999L, 12L, false))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        mockMvc.perform(get("/api/v1/solicitudes/999")
                        .header("Authorization", "Bearer " + token("12")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));
    }

    @Test
    void clientCreatesAppointmentUnderIdentityFromBearerToken() throws Exception {
        when(usuarioRepository.findById(8L))
                .thenReturn(Optional.of(usuario(8L, "CLIENTE")));
        when(solicitudService.crear(eq(8L), any())).thenReturn(response(15L));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/solicitudes")
                        .header("Authorization", "Bearer " + token("8"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mascota": "Luna",
                                  "fechaHora": "2026-12-10T10:30:00",
                                  "motivo": "Control anual"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15));

        org.mockito.Mockito.verify(solicitudService).crear(eq(8L), any(SolicitudRequest.class));
    }

    @Test
    void clientCannotReadAnotherClientsAppointment() throws Exception {
        when(usuarioRepository.findById(8L))
                .thenReturn(Optional.of(usuario(8L, "CLIENTE")));
        when(solicitudService.obtenerPorId(99L, 8L, true))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        mockMvc.perform(get("/api/v1/solicitudes/99")
                        .header("Authorization", "Bearer " + token("8")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));

        verify(solicitudService).obtenerPorId(99L, 8L, true);
    }

    private Usuario usuario(Long id, String rol) {
        return Usuario.builder()
                .id(id)
                .nombre("Usuario " + id)
                .correo("usuario" + id + "@example.com")
                .rol(rol)
                .activo(true)
                .build();
    }

    private SolicitudResponse response(Long id) {
        return SolicitudResponse.builder()
                .id(id)
                .tipo("CITA")
                .estado(SolicitudEstado.CONFIRMADA)
                .build();
    }

    private String token(String subject) {
        Instant now = Instant.now();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer("test-issuer")
                        .subject(subject)
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(60))
                        .build())).getTokenValue();
    }
}
