package cl.veterinariasanmarcos.veterinaria_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.ProductoRequest;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.ProductoMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Producto;
import cl.veterinariasanmarcos.veterinaria_backend.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, new ProductoMapper());
    }

    @Test
    void crearProductoMapeaCamposAdministrativosYPublicos() {
        ProductoRequest request = request();
        request.setDescripcion("Alimento para perro");
        request.setImagen("https://example.com/alimento.jpg");
        when(productoRepository.saveAndFlush(any(Producto.class)))
                .thenAnswer(invocation -> {
                    Producto producto = invocation.getArgument(0);
                    producto.setId(5L);
                    return producto;
                });

        var response = productoService.crear(request);

        assertEquals(5L, response.getId());
        assertEquals("Alimento premium", response.getNombre());
        assertEquals("Alimento", response.getCategoria());
        assertEquals("Alimento para perro", response.getDescripcion());
        assertEquals(new BigDecimal("24990"), response.getPrecio());
        assertEquals("https://example.com/alimento.jpg", response.getImagen());
        assertEquals(10, response.getStock());
    }

    @Test
    void actualizarConservaDescripcionEImagenNoIncluidasEnElFormularioAdmin() {
        Producto existente = Producto.builder()
                .id(5L)
                .nombre("Anterior")
                .categoria("Alimento")
                .descripcion("Descripción pública")
                .imagen("https://example.com/anterior.jpg")
                .precio(new BigDecimal("1000"))
                .stock(1)
                .build();
        when(productoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(productoRepository.saveAndFlush(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = productoService.actualizar(5L, request());

        assertEquals("Alimento premium", response.getNombre());
        assertEquals("Descripción pública", response.getDescripcion());
        assertEquals("https://example.com/anterior.jpg", response.getImagen());
    }

    @Test
    void actualizarProductoInexistenteDevuelve404() {
        when(productoRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productoService.actualizar(404L, request()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private ProductoRequest request() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre(" Alimento premium ");
        request.setCategoria(" Alimento ");
        request.setPrecio(new BigDecimal("24990"));
        request.setStock(10);
        return request;
    }
}
