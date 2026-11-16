package cl.veterinariasanmarcos.veterinaria_backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder 
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String correo;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}