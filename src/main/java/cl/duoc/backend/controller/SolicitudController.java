package cl.duoc.backend.controller;

import cl.duoc.backend.dto.SolicitudRequest;
import cl.duoc.backend.model.Solicitud;
import cl.duoc.backend.repository.SolicitudRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {

    private final SolicitudRepository solicitudRepository;

    public SolicitudController(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    @GetMapping
    public List<Solicitud> listarSolicitudes() {
        return solicitudRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Solicitud> crearSolicitud(@Valid @RequestBody SolicitudRequest req) {
        Solicitud nueva = new Solicitud();
        nueva.setCliente(req.cliente().trim());
        nueva.setServicio(req.servicio().trim());
        nueva.setFecha(
            (req.fecha() != null && !req.fecha().isBlank())
                ? req.fecha().trim()
                : LocalDate.now().toString()
        );
        nueva.setEstado(
            (req.estado() != null && !req.estado().isBlank())
                ? req.estado().trim()
                : "Pendiente"
        );
        nueva.setTotal(req.total() != null ? req.total() : 0);

        Solicitud guardada = solicitudRepository.save(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarSolicitud(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return solicitudRepository.findById(id)
                .map(s -> {
                    if (body.containsKey("estado") && body.get("estado") != null) {
                        s.setEstado(String.valueOf(body.get("estado")));
                    }
                    if (body.containsKey("servicio") && body.get("servicio") != null) {
                        s.setServicio(String.valueOf(body.get("servicio")));
                    }
                    if (body.containsKey("fecha") && body.get("fecha") != null) {
                        s.setFecha(String.valueOf(body.get("fecha")));
                    }
                    return ResponseEntity.ok(solicitudRepository.save(s));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarSolicitud(@PathVariable Long id) {
        if (!solicitudRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        solicitudRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Solicitud eliminada"));
    }
}
