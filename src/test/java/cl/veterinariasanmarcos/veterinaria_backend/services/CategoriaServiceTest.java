package cl.veterinariasanmarcos.veterinaria_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.CategoriaRequest;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.CategoriaMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Categoria;
import cl.veterinariasanmarcos.veterinaria_backend.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository, new CategoriaMapper());
    }

    @Test
    void crearNormalizaNombreYDevuelveLaCategoriaPersistida() {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("  Alimento  ");
        request.setDescripcion(" Alimento para mascotas ");
        when(categoriaRepository.existsByNombreIgnoreCase("Alimento")).thenReturn(false);
        when(categoriaRepository.saveAndFlush(any(Categoria.class)))
                .thenAnswer(invocation -> {
                    Categoria categoria = invocation.getArgument(0);
                    categoria.setId(3L);
                    return categoria;
                });

        var response = categoriaService.crear(request);

        assertEquals(3L, response.getId());
        assertEquals("Alimento", response.getNombre());
        assertEquals("Alimento para mascotas", response.getDescripcion());
    }

    @Test
    void crearCategoriaDuplicadaDevuelve409() {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("ALIMENTO");
        when(categoriaRepository.existsByNombreIgnoreCase("ALIMENTO")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.crear(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void eliminarCategoriaInexistenteDevuelve404() {
        when(categoriaRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.eliminar(404L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
