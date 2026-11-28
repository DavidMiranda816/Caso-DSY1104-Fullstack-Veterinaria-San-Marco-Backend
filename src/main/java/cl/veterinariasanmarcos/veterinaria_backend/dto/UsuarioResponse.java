package cl.veterinariasanmarcos.veterinaria_backend.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder 
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Perfil seguro de usuario. No contiene contraseña ni hash.")
public class UsuarioResponse {

    @Schema(description = "Identificador de usuario.", example = "42",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del usuario.", example = "Juan Pérez",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String nombre;

    @Schema(description = "Correo de la cuenta.", example = "juan@example.com",
            format = "email", accessMode = Schema.AccessMode.READ_ONLY)
    private String correo;

    @Schema(description = "Rol vigente de la cuenta.", example = "CLIENTE",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String rol;

    @Schema(description = "Indica si la cuenta está activa.", example = "true",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean activo;

    @Schema(description = "Fecha y hora de creación de la cuenta.",
            example = "2026-01-15T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;
}