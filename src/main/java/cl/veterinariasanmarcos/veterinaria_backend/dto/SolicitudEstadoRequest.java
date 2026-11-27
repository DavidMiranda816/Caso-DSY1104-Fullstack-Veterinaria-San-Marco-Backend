package cl.veterinariasanmarcos.veterinaria_backend.dto;

import java.time.LocalDateTime;

import cl.veterinariasanmarcos.veterinaria_backend.model.SolicitudEstado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Cambio de estado de una cita.")
public class SolicitudEstadoRequest {

    @NotNull
    @Schema(description = "Estado solicitado.", example = "CONFIRMADA",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private SolicitudEstado estado;

    @Future
    @Schema(description = "Nueva fecha/hora obligatoria para REPROGRAMADA.",
            example = "2026-12-15T11:00:00")
    private LocalDateTime fechaHora;
}
