package cl.veterinariasanmarcos.veterinaria_backend.mapper;

import org.springframework.stereotype.Component;

import cl.veterinariasanmarcos.veterinaria_backend.dto.ProductoRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.ProductoResponse;
import cl.veterinariasanmarcos.veterinaria_backend.model.Producto;

@Component
public class ProductoMapper {

    public Producto fromRequest(ProductoRequest request) {
        return Producto.builder()
                .nombre(request.getNombre())
                .categoria(request.getCategoria())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .imagen(request.getImagen())
                .stock(request.getStock())
                .build();
    }

    public ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .categoria(producto.getCategoria())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .imagen(producto.getImagen())
                .stock(producto.getStock())
                .build();
    }
}
