package cl.veterinariasanmarcos.veterinaria_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioUpdateRequest;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.UsuarioMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

@Service
@Transactional 
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public List<UsuarioResponse> listarUsuarios() {
        List<UsuarioResponse> respuestas = new ArrayList<>();

        for (Usuario usuario : usuarioRepository.findAll()) {
            respuestas.add(usuarioMapper.toResponse(usuario));
        }

        return respuestas;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return usuarioMapper.toResponse(buscarUsuario(id));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {

        Usuario usuario = buscarUsuario(id);

        String correo = request.getCorreo().trim().toLowerCase(Locale.ROOT);

        Usuario existente = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElse(null);

        if (existente != null && !existente.getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya está registrado");
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        return usuarioMapper.toResponse(guardado);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscarUsuario(id);

        usuario.setActivo(false);
        usuarioRepository.saveAndFlush(usuario);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));
    }
}