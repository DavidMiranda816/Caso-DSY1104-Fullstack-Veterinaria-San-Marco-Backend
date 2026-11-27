package cl.veterinariasanmarcos.veterinaria_backend.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de una cita.")
public enum SolicitudEstado {
    PENDIENTE,
    CONFIRMADA,
    REPROGRAMADA,
    COMPLETADA,
    CANCELADA
}
