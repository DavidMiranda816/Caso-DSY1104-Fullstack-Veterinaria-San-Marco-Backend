package cl.veterinariasanmarcos.veterinaria_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.veterinariasanmarcos.veterinaria_backend.dto.CategoriaRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.CategoriaResponse;
import cl.veterinariasanmarcos.veterinaria_backend.services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/categorias", "/api/v1/categorias"})
@Tag(name = "Categorías", description = "Catálogo de categorías de productos.")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar categorías",
            description = "Requiere sesión vigente; disponible para cualquier rol autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de categorías",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = CategoriaResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Sesión inválida")
    })
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Consultar categoría por ID",
            description = "Requiere sesión vigente; disponible para cualquier rol autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(
                            implementation = CategoriaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public CategoriaResponse obtenerPorId(
            @Parameter(description = "Identificador de categoría.", example = "1")
            @PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear categoría", description = "Operación reservada a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada",
                    content = @Content(schema = @Schema(
                            implementation = CategoriaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    })
    public ResponseEntity<CategoriaResponse> crear(
            @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaService.crear(request));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar categoría", description = "Operación reservada a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada",
                    content = @Content(schema = @Schema(
                            implementation = CategoriaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    })
    public CategoriaResponse actualizar(
            @Parameter(description = "Identificador de categoría.", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar categoría", description = "Operación reservada a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador de categoría.", example = "1")
            @PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
