package cl.veterinariasanmarcos.veterinaria_backend.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.ProductoRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.ProductoResponse;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.ProductoMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Producto;
import cl.veterinariasanmarcos.veterinaria_backend.repository.ProductoRepository;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(
            ProductoRepository productoRepository,
            ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return productoMapper.toResponse(buscar(id));
    }

    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = productoMapper.fromRequest(request);
        normalizar(producto);
        return productoMapper.toResponse(productoRepository.saveAndFlush(producto));
    }

    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        producto.setNombre(request.getNombre());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());

        if (request.getDescripcion() != null) {
            producto.setDescripcion(request.getDescripcion());
        }
        if (request.getImagen() != null) {
            producto.setImagen(request.getImagen());
        }
        normalizar(producto);

        return productoMapper.toResponse(productoRepository.saveAndFlush(producto));
    }

    public void eliminar(Long id) {
        productoRepository.delete(buscar(id));
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));
    }

    private void normalizar(Producto producto) {
        producto.setNombre(producto.getNombre().trim());
        producto.setCategoria(producto.getCategoria().trim());
        if (producto.getDescripcion() != null) {
            producto.setDescripcion(producto.getDescripcion().trim());
        }
        if (producto.getImagen() != null) {
            producto.setImagen(producto.getImagen().trim());
        }
    }
}
