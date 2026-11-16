package cl.veterinariasanmarcos.veterinaria_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @ToString.Exclude
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}