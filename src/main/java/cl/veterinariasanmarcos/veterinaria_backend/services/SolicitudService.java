package cl.veterinariasanmarcos.veterinaria_backend.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudEstadoRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudResponse;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.SolicitudMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Solicitud;
import cl.veterinariasanmarcos.veterinaria_backend.model.SolicitudEstado;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.SolicitudRepository;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

@Service
@Transactional
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMapper solicitudMapper;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            SolicitudMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudMapper = solicitudMapper;
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(Long actorId, boolean cliente) {
        List<Solicitud> solicitudes = cliente
                ? solicitudRepository.findAllByClienteIdOrderByFechaHoraAsc(actorId)
                : solicitudRepository.findAllByOrderByFechaHoraAsc();
        return solicitudes.stream().map(solicitudMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtenerPorId(Long id, Long actorId, boolean cliente) {
        Solicitud solicitud = cliente
                ? solicitudRepository.findByIdAndClienteId(id, actorId)
                        .orElseThrow(this::noEncontrada)
                : solicitudRepository.findById(id).orElseThrow(this::noEncontrada);
        return solicitudMapper.toResponse(solicitud);
    }

    public SolicitudResponse crear(Long actorId, SolicitudRequest request) {
        Usuario cliente = usuarioRepository.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "La sesión no es válida"));

        Solicitud solicitud = new Solicitud();
        solicitud.setCliente(cliente);
        solicitud.setMascota(request.getMascota().trim());
        solicitud.setFechaHora(request.getFechaHora());
        solicitud.setMotivo(request.getMotivo().trim());
        solicitud.setEstado(SolicitudEstado.PENDIENTE);

        return solicitudMapper.toResponse(solicitudRepository.save(solicitud));
    }

    public SolicitudResponse cambiarEstado(Long id, SolicitudEstadoRequest request) {
        Solicitud solicitud = solicitudRepository.findByIdForUpdate(id)
                .orElseThrow(this::noEncontrada);
        SolicitudEstado actual = solicitud.getEstado();
        SolicitudEstado siguiente = request.getEstado();

        if (!transicionPermitida(actual, siguiente)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cambiar una cita de " + actual + " a " + siguiente);
        }

        if (siguiente == SolicitudEstado.REPROGRAMADA) {
            if (request.getFechaHora() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debe indicar la nueva fecha y hora para reprogramar");
            }
            solicitud.setFechaHora(request.getFechaHora());
        } else if (request.getFechaHora() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva fecha y hora solo se admite al reprogramar");
        }

        solicitud.setEstado(siguiente);
        return solicitudMapper.toResponse(solicitudRepository.saveAndFlush(solicitud));
    }

    public void eliminar(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(this::noEncontrada);
        solicitudRepository.delete(solicitud);
    }

    private boolean transicionPermitida(
            SolicitudEstado actual,
            SolicitudEstado siguiente) {
        return switch (actual) {
            case PENDIENTE -> siguiente == SolicitudEstado.CONFIRMADA
                    || siguiente == SolicitudEstado.REPROGRAMADA
                    || siguiente == SolicitudEstado.CANCELADA;
            case CONFIRMADA -> siguiente == SolicitudEstado.REPROGRAMADA
                    || siguiente == SolicitudEstado.COMPLETADA
                    || siguiente == SolicitudEstado.CANCELADA;
            case REPROGRAMADA -> siguiente == SolicitudEstado.CONFIRMADA
                    || siguiente == SolicitudEstado.CANCELADA;
            case COMPLETADA, CANCELADA -> false;
        };
    }

    private ResponseStatusException noEncontrada() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Solicitud no encontrada");
    }
}
