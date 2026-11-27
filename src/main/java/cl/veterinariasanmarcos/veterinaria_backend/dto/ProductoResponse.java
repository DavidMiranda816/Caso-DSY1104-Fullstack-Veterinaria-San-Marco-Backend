package cl.veterinariasanmarcos.veterinaria_backend.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Producto del catálogo con los campos usados por el frontend.")
public class ProductoResponse {

    @Schema(description = "Identificador numérico del producto.", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del producto.", example = "Alimento premium 15 kg")
    private String nombre;

    @Schema(description = "Nombre de la categoría.", example = "Alimento")
    private String categoria;

    @Schema(description = "Descripción del producto.",
            example = "Alimento para perro adulto")
    private String descripcion;

    @Schema(description = "Precio en pesos chilenos.", example = "24990")
    private BigDecimal precio;

    @Schema(description = "URL de la imagen del producto.",
            example = "https://example.com/producto.jpg")
    private String imagen;

    @Schema(description = "Unidades disponibles en inventario.", example = "10")
    private Integer stock;
}
