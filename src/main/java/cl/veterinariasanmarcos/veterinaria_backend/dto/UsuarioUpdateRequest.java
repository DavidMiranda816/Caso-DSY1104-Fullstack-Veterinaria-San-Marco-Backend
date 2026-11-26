package cl.veterinariasanmarcos.veterinaria_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Campos editables de una cuenta de usuario.")
public class UsuarioUpdateRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nombre del usuario.", example = "Juan Pérez",
            maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 254)
    @Schema(description = "Correo único de la cuenta.", example = "juan@example.com",
            format = "email", maxLength = 254,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String correo;
}