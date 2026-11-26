package cl.veterinariasanmarcos.veterinaria_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(description = "Datos para crear una cuenta con rol ADMIN.")
public class AdministradorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    @Schema(description = "Nombre del administrador.", example = "Ana Pérez",
            maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 254, message = "El correo admite hasta 254 caracteres")
    @Schema(description = "Correo único de la cuenta.", example = "ana@example.com",
            format = "email", maxLength = 254,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String correo;

    @ToString.Exclude
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Contraseña de al menos 8 caracteres y hasta 72 bytes.",
            example = "ClaveSegura123", format = "password", minLength = 8,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
