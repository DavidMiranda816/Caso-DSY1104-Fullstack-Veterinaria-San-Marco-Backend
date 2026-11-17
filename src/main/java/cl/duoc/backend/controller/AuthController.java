package cl.duoc.backend.controller;

import cl.duoc.backend.dto.LoginRequest;
import cl.duoc.backend.dto.RegistroRequest;
import cl.duoc.backend.model.Rol;
import cl.duoc.backend.model.Usuario;
import cl.duoc.backend.repository.UsuarioRepository;
import cl.duoc.backend.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest req) {
        if (usuarioRepository.existsByEmail(req.email().trim().toLowerCase())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El correo ya está registrado"));
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(req.nombre().trim());
        nuevo.setEmail(req.email().trim().toLowerCase());
        nuevo.setPassword(passwordEncoder.encode(req.password()));
        nuevo.setRol(Rol.DUENO);
        nuevo.setActivo(true);

        Usuario guardado = usuarioRepository.save(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return usuarioRepository.findByEmail(req.email().trim().toLowerCase())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> passwordEncoder.matches(req.password(), u.getPassword()))
                .map(u -> {
                    String token = jwtService.generarToken(u);
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "tipo", "Bearer",
                            "usuario", Map.of(
                                    "id", u.getId(),
                                    "nombre", u.getNombre(),
                                    "email", u.getEmail(),
                                    "rol", u.getRol()
                            )
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas")));
    }
}
