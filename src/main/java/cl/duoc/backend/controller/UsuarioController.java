package cl.duoc.backend.controller;

import cl.duoc.backend.model.Usuario;
import cl.duoc.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarUsuario(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    if (body.containsKey("nombre")) u.setNombre(body.get("nombre"));
                    return ResponseEntity.ok(usuarioRepository.save(u));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    u.setActivo(false);
                    usuarioRepository.save(u);
                    return ResponseEntity.ok(Map.of("mensaje", "Usuario desactivado"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
