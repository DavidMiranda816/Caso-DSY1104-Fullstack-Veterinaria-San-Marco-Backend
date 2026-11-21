package cl.veterinariasanmarcos.veterinaria_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import cl.veterinariasanmarcos.veterinaria_backend.dto.AdministradorRequest;
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
}
