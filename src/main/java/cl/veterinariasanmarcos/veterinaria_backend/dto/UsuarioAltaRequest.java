package cl.veterinariasanmarcos.veterinaria_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para crear una cuenta de Kaiju desde la administración de usuarios.")
public class UsuarioAltaRequest extends UsuarioRequest {

    @NotNull(message = "El rol es obligatorio")
    @Schema(description = "Perfil de Kaiju.", example = "VENDEDOR",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private RolKaiju rol;

    public enum RolKaiju {
        ADMIN,
        VENDEDOR,
        ENCARGADO
    }
}
