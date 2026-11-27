package cl.veterinariasanmarcos.veterinaria_backend.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para solicitar una cita. El cliente se obtiene del JWT.")
public class SolicitudRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nombre de la mascota.", example = "Luna",
            maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String mascota;

    @NotNull
    @Future
    @Schema(description = "Fecha y hora local de la cita, en formato ISO-8601.",
            example = "2026-12-10T10:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime fechaHora;

    @NotBlank
    @Size(max = 500)
    @Schema(description = "Motivo de la cita.", example = "Control anual",
            maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;
}
