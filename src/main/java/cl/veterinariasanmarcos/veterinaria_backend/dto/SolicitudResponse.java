package cl.veterinariasanmarcos.veterinaria_backend.dto;

import java.time.LocalDateTime;

import cl.veterinariasanmarcos.veterinaria_backend.model.SolicitudEstado;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Cita y datos de su cliente. Los pedidos del carrito no se incluyen.")
public class SolicitudResponse {

    private Long id;
    private String tipo;
    private Long clienteId;
    private String clienteNombre;
    private String clienteCorreo;
    private String mascota;
    private LocalDateTime fechaHora;
    private String motivo;
    private SolicitudEstado estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
