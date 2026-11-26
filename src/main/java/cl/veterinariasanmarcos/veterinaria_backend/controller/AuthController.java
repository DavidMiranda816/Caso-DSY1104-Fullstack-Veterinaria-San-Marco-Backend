package cl.veterinariasanmarcos.veterinaria_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.veterinariasanmarcos.veterinaria_backend.dto.LoginRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.LoginResponse;
import cl.veterinariasanmarcos.veterinaria_backend.dto.AdministradorRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.UsuarioResponse;
import cl.veterinariasanmarcos.veterinaria_backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, inicio y revalidación de sesión.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(
            summary = "Registrar una cuenta pública",
            description = "Crea la cuenta con el rol CLIENTE, asignado por el servidor.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cuenta creada",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody UsuarioRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registrar(request));
    }

    @PostMapping("/admin/registro")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Registrar un administrador",
            description = "Requiere un JWT vigente cuya cuenta tenga actualmente el rol ADMIN.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Administrador creado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Sesión inválida"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    public ResponseEntity<UsuarioResponse> registrarAdministrador(
            @Valid @RequestBody AdministradorRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registrarAdministrador(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión y obtener JWT",
            description = "Devuelve un JWT Bearer y el perfil seguro de la cuenta.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación correcta",
                    content = @Content(
                            schema = @Schema(
                                    implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Revalidar la sesión y consultar el usuario actual",
            description = "Confirma que la cuenta existe y sigue activa; acepta cualquier rol autenticado.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión vigente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Sesión inválida o cuenta inactiva")
    })
    public UsuarioResponse obtenerUsuarioActual(
            @AuthenticationPrincipal Jwt jwt) {

        return authService.obtenerUsuarioActual(jwt.getSubject());
    }
}