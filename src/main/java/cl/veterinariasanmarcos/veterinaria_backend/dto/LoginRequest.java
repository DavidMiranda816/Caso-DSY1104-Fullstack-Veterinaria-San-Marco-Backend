package cl.veterinariasanmarcos.veterinaria_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(description = "Credenciales para iniciar sesión.")
public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Schema(description = "Correo de la cuenta.", example = "ana@example.com",
            format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String correo;

    @ToString.Exclude
    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña de la cuenta.", example = "ClaveSegura123",
            format = "password", accessMode = Schema.AccessMode.WRITE_ONLY,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}