package cl.duoc.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudRequest(
    @NotBlank(message = "El cliente es obligatorio") String cliente,
    @NotBlank(message = "El detalle del servicio o pedido es obligatorio") String servicio,
    String fecha,
    String estado,
    Integer total
) {}
