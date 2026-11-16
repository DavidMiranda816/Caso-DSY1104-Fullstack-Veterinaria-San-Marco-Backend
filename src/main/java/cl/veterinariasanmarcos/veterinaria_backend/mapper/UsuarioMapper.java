package cl.veterinariasanmarcos.veterinaria_backend.mapper;

import org.springframework.stereotype.Component;

import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;

@Component
public class UsuarioMapper {

    public Usuario fromRequest(UsuarioRequest request) {
        return Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .build();
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}