package cl.veterinariasanmarcos.veterinaria_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para crear o actualizar una categoría.")
public class CategoriaRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nombre de la categoría.", example = "Alimento",
            maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Size(max = 500)
    @Schema(description = "Descripción de la categoría.",
            example = "Alimento para mascotas", maxLength = 500)
    private String descripcion;
}
