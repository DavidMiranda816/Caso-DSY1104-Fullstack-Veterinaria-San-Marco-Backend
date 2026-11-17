package cl.veterinariasanmarcos.veterinaria_backend.services;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.LoginRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.LoginResponse;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.UsuarioMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashFicticio;

    public AuthService(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

        this.hashFicticio = passwordEncoder.encode(
                "Comparacion-interna-sin-cuenta");
    }

    @Transactional
    public UsuarioResponse registrar(UsuarioRequest request) {
        String correo = normalizarCorreo(request.getCorreo());

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya está registrado");
        }

        if (request.getPassword()
                .getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña no puede superar 72 bytes");
        }

        Usuario usuario = usuarioMapper.fromRequest(request);

        
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setRol("CLIENTE");
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setActivo(true);
        usuario.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        return usuarioMapper.toResponse(guardado);
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getPassword()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw credencialesIncorrectas();
        }

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(
                        normalizarCorreo(request.getCorreo()))
                .orElse(null);

        String hash = usuario == null
                ? hashFicticio
                : usuario.getPasswordHash();

        boolean passwordCorrecta = passwordEncoder.matches(
                request.getPassword(), hash);

        if (usuario == null
                || !passwordCorrecta
                || !Boolean.TRUE.equals(usuario.getActivo())) {

            throw credencialesIncorrectas();
        }

        return LoginResponse.builder()
                .token(jwtService.generarToken(usuario))
                .tipo("Bearer")
                .usuario(usuarioMapper.toResponse(usuario))
                .build();
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException credencialesIncorrectas() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos");
    }
}