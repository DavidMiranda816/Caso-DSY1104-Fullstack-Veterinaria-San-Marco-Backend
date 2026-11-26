package cl.veterinariasanmarcos.veterinaria_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "JWT y perfil seguro del usuario autenticado.")
public class LoginResponse {

    @Schema(description = "JWT firmado para usar como Bearer token.",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0MiJ9.signature")
    private String token;

    @Schema(description = "Esquema de autenticación.", example = "Bearer")
    private String tipo;

    @Schema(description = "Datos seguros de la cuenta autenticada.")
    private UsuarioResponse usuario;
}