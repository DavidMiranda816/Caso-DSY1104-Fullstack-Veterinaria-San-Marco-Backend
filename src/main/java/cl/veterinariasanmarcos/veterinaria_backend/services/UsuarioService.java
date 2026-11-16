package cl.veterinariasanmarcos.veterinaria_backend.services;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.UsuarioMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public List<UsuarioResponse> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioResponse> respuestas = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            respuestas.add(usuarioMapper.toResponse(usuario));
        }

        return respuestas;
    }
}
