package cl.veterinariasanmarcos.veterinaria_backend.mapper;

import org.springframework.stereotype.Component;

import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudResponse;
import cl.veterinariasanmarcos.veterinaria_backend.model.Solicitud;

@Component
public class SolicitudMapper {

    public SolicitudResponse toResponse(Solicitud solicitud) {
        return SolicitudResponse.builder()
                .id(solicitud.getId())
                .tipo("CITA")
                .clienteId(solicitud.getCliente().getId())
                .clienteNombre(solicitud.getCliente().getNombre())
                .clienteCorreo(solicitud.getCliente().getCorreo())
                .mascota(solicitud.getMascota())
                .fechaHora(solicitud.getFechaHora())
                .motivo(solicitud.getMotivo())
                .estado(solicitud.getEstado())
                .fechaCreacion(solicitud.getFechaCreacion())
                .fechaActualizacion(solicitud.getFechaActualizacion())
                .build();
    }
}
