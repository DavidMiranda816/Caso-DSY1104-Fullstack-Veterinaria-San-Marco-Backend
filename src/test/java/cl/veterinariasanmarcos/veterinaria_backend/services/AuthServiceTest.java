package cl.veterinariasanmarcos.veterinaria_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.AdministradorRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioAltaRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioRequest;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.UsuarioMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                usuarioRepository,
                new UsuarioMapper(),
                new BCryptPasswordEncoder(),
                jwtService);
    }

    @Test
    void registroPublicoSiempreCreaCliente() {
        when(usuarioRepository.existsByCorreoIgnoreCase("cliente@example.com"))
                .thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Cliente");
        request.setCorreo("cliente@example.com");
        request.setPassword("contrasena-segura");

        assertEquals("CLIENTE", authService.registrar(request).getRol());
        verify(usuarioRepository).saveAndFlush(
                argThat(usuario -> "CLIENTE".equals(usuario.getRol())));
    }

    @Test
    void registroSeparadoCreaAdministrador() {
        when(usuarioRepository.existsByCorreoIgnoreCase("admin@example.com"))
                .thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdministradorRequest request = new AdministradorRequest();
        request.setNombre("Admin");
        request.setCorreo("admin@example.com");
        request.setPassword("contrasena-segura");

        assertEquals(
                "ADMIN",
                authService.registrarAdministrador(request).getRol());
        verify(usuarioRepository).saveAndFlush(
                argThat(usuario -> "ADMIN".equals(usuario.getRol())));
    }

    @ParameterizedTest
    @EnumSource(UsuarioAltaRequest.RolKaiju.class)
    void altaKaijuGuardaPerfilActivoYHashDeContrasena(UsuarioAltaRequest.RolKaiju rol) {
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    assertTrue(new BCryptPasswordEncoder()
                            .matches("contrasena-segura", usuario.getPasswordHash()));
                    assertEquals("kaiju@example.com", usuario.getCorreo());
                    return usuario;
                });

        UsuarioAltaRequest request = new UsuarioAltaRequest();
        request.setNombre("Cuenta de Kaiju");
        request.setCorreo("KAIJU@example.com");
        request.setPassword("contrasena-segura");
        request.setRol(rol);

        var respuesta = authService.registrarUsuarioKaiju(request);
        assertEquals(rol.name(), respuesta.getRol());
        assertTrue(respuesta.getActivo());
    }

    @Test
    void obtenerUsuarioActualDevuelveLaCuentaVigente() {
        Usuario usuario = Usuario.builder()
                .id(23L)
                .nombre("Cliente")
                .correo("cliente@example.com")
                .rol("CLIENTE")
                .activo(true)
                .build();
        when(usuarioRepository.findById(23L)).thenReturn(Optional.of(usuario));

        assertEquals(
                "cliente@example.com",
                authService.obtenerUsuarioActual("23").getCorreo());
    }

    @Test
    void obtenerUsuarioActualRechazaCuentaDesactivada() {
        Usuario usuario = Usuario.builder()
                .id(23L)
                .activo(false)
                .build();
        when(usuarioRepository.findById(23L)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.obtenerUsuarioActual("23"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void obtenerUsuarioActualRechazaCuentaInexistente() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.obtenerUsuarioActual("404"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void obtenerUsuarioActualRechazaUnSubjectNoNumerico() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.obtenerUsuarioActual("invalido"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
