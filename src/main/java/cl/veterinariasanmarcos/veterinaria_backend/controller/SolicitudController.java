package cl.veterinariasanmarcos.veterinaria_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudEstadoRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudResponse;
import cl.veterinariasanmarcos.veterinaria_backend.services.SolicitudService;
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
@RequestMapping("/api/v1/solicitudes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Solicitudes (citas)", description = "Gestión de citas; no incluye pedidos del carrito.")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    @Operation(summary = "Listar citas",
            description = "ADMIN y RECEPCIONISTA ven todas. CLIENTE solo ve las propias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de citas",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = SolicitudResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Rol sin acceso")
    })
    public List<SolicitudResponse> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        return solicitudService.listar(
                Long.valueOf(jwt.getSubject()),
                tieneRol(authentication, "CLIENTE"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una cita por folio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita encontrada",
                    content = @Content(schema = @Schema(
                            implementation = SolicitudResponse.class))),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Rol sin acceso"),
            @ApiResponse(responseCode = "404", description = "Folio no encontrado")
    })
    public SolicitudResponse obtenerPorId(
            @Parameter(description = "Folio de la cita.", example = "15")
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        return solicitudService.obtenerPorId(
                id,
                Long.valueOf(jwt.getSubject()),
                tieneRol(authentication, "CLIENTE"));
    }

    @PostMapping
    @Operation(summary = "Crear una cita",
            description = "Disponible para CLIENTE; el ID de cliente siempre se toma del JWT. "
                    + "No crea pedidos de carrito ni genera un conflicto de correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cita creada",
                    content = @Content(schema = @Schema(
                            implementation = SolicitudResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado")
    })
    public ResponseEntity<SolicitudResponse> crear(
            @Valid @RequestBody SolicitudRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.crear(
                        Long.valueOf(jwt.getSubject()), request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de una cita",
            description = "ADMIN y RECEPCIONISTA pueden gestionar citas. "
                    + "Transiciones: PENDIENTE a CONFIRMADA, REPROGRAMADA o CANCELADA; "
                    + "CONFIRMADA a REPROGRAMADA, COMPLETADA o CANCELADA; "
                    + "REPROGRAMADA a CONFIRMADA o CANCELADA. COMPLETADA y CANCELADA "
                    + "son terminales. Reprogramar requiere una nueva fecha/hora. "
                    + "La validación y actualización ocurren en una transacción.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(
                            implementation = SolicitudResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Folio no encontrado"),
            @ApiResponse(responseCode = "409",
                    description = "Transición incompatible con el estado actual")
    })
    public SolicitudResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudEstadoRequest request) {
        return solicitudService.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una cita",
            description = "Operación reservada a ADMIN. Los pedidos de carrito no forman parte de este módulo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cita eliminada"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Folio no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        solicitudService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + rol));
    }
}
