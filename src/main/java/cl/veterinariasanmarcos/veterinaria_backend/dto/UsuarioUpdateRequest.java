package cl.veterinariasanmarcos.veterinaria_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Campos editables de una cuenta de usuario.")
public class UsuarioUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    @Schema(description = "Nombre del usuario.", example = "Juan Pérez",
            maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 254, message = "El correo admite hasta 254 caracteres")
    @Schema(description = "Correo único de la cuenta.", example = "juan@example.com",
            format = "email", maxLength = 254,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String correo;
}