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
@Schema(description = "Categoría disponible en el catálogo.")
public class CategoriaResponse {

    @Schema(description = "Identificador de la categoría.", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre de la categoría.", example = "Alimento")
    private String nombre;

    @Schema(description = "Descripción de la categoría.",
            example = "Alimento para mascotas")
    private String descripcion;
}
