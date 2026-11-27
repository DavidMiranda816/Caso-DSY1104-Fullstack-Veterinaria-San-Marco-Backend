package cl.veterinariasanmarcos.veterinaria_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioAltaRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioUpdateRequest;
import cl.veterinariasanmarcos.veterinaria_backend.services.AuthService;
import cl.veterinariasanmarcos.veterinaria_backend.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administración de usuarios", description = "Operaciones disponibles solo para ADMIN.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Crear una cuenta de Kaiju con su perfil")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos o perfil inválidos"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioAltaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registrarUsuarioKaiju(request));
    }

    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Devuelve perfiles seguros sin contraseñas ni hashes.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de usuarios",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = UsuarioResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado")
    })
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar usuario por ID",
            description = "Devuelve el mismo perfil seguro utilizado por el listado.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public UsuarioResponse obtenerPorId(
            @Parameter(description = "Identificador del usuario.", example = "42")
            @PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar nombre y correo de un usuario",
            description = "Actualiza únicamente los campos editables enviados.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    public UsuarioResponse actualizar(
            @Parameter(description = "Identificador del usuario.", example = "42")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {

        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(
            summary = "Desactivar una cuenta de usuario",
            description = "Desactiva la cuenta; sus JWT dejan de autorizar peticiones.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta desactivada"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "No puedes desactivar tu propia cuenta")
    })
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "Identificador del usuario.", example = "42")
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        usuarioService.desactivar(id, jwt.getSubject());

        return ResponseEntity.noContent().build();
    }
}