package cl.veterinariasanmarcos.veterinaria_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioUpdateRequest;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.UsuarioMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, new UsuarioMapper());
    }

    @Test
    void obtenerPorIdEntregaElDtoSeguroDelUsuario() {
        Usuario usuario = Usuario.builder()
                .id(17L)
                .nombre("Admin")
                .correo("admin@example.com")
                .passwordHash("hash-privado")
                .rol("ADMIN")
                .activo(true)
                .fechaCreacion(LocalDateTime.parse("2026-01-02T03:04:05"))
                .build();
        when(usuarioRepository.findById(17L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.obtenerPorId(17L);

        assertEquals(17L, response.getId());
        assertEquals("Admin", response.getNombre());
        assertEquals("admin@example.com", response.getCorreo());
        assertEquals("ADMIN", response.getRol());
        assertEquals(true, response.getActivo());
        assertEquals(usuario.getFechaCreacion(), response.getFechaCreacion());
    }

    @Test
    void obtenerPorIdInexistenteEntrega404() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.obtenerPorId(404L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void actualizarPuedeAsignarRolRecepcionista() {
        Usuario usuario = Usuario.builder()
                .id(17L)
                .nombre("Usuario")
                .correo("usuario@example.com")
                .rol("CLIENTE")
                .activo(true)
                .build();
        when(usuarioRepository.findById(17L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreoIgnoreCase("recepcion@example.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("Recepcionista");
        request.setCorreo("recepcion@example.com");
        request.setRol("RECEPCIONISTA");

        UsuarioResponse response = usuarioService.actualizar(17L, request);

        assertEquals("RECEPCIONISTA", response.getRol());
    }

    @Test
    void actualizarSinRolConservaElRolActual() {
        Usuario usuario = Usuario.builder()
                .id(17L)
                .nombre("Usuario")
                .correo("usuario@example.com")
                .rol("VENDEDOR")
                .activo(true)
                .build();
        when(usuarioRepository.findById(17L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreoIgnoreCase("usuario@example.com"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("Usuario actualizado");
        request.setCorreo("usuario@example.com");

        UsuarioResponse response = usuarioService.actualizar(17L, request);

        assertEquals("VENDEDOR", response.getRol());
    }
}
