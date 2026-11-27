package cl.veterinariasanmarcos.veterinaria_backend.mapper;

import org.springframework.stereotype.Component;

import cl.veterinariasanmarcos.veterinaria_backend.dto.CategoriaRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.CategoriaResponse;
import cl.veterinariasanmarcos.veterinaria_backend.model.Categoria;

@Component
public class CategoriaMapper {

    public Categoria fromRequest(CategoriaRequest request) {
        return Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
    }

    public CategoriaResponse toResponse(Categoria categoria) {
        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }
}
