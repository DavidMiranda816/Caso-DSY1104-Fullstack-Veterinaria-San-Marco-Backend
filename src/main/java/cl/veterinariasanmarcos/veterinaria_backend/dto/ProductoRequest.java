package cl.veterinariasanmarcos.veterinaria_backend.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para crear o actualizar un producto del catálogo.")
public class ProductoRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nombre del producto.", example = "Alimento premium 15 kg",
            maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nombre de la categoría, según el formulario actual.",
            example = "Alimento", maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoria;

    @Size(max = 1000)
    @Schema(description = "Descripción visible en el catálogo.",
            example = "Alimento para perro adulto", maxLength = 1000)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Precio en pesos chilenos.", example = "24990",
            minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precio;

    @Size(max = 2048)
    @Schema(description = "URL de la imagen del producto.",
            example = "https://example.com/producto.jpg", maxLength = 2048)
    private String imagen;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Unidades disponibles en inventario.", example = "10",
            minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;
}
