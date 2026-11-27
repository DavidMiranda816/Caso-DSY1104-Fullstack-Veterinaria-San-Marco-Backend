package cl.veterinariasanmarcos.veterinaria_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudEstadoRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.SolicitudResponse;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.SolicitudMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Solicitud;
import cl.veterinariasanmarcos.veterinaria_backend.model.SolicitudEstado;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.SolicitudRepository;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;

class SolicitudServiceTest {

    private SolicitudRepository solicitudRepository;
    private SolicitudMapper solicitudMapper;
    private SolicitudService solicitudService;

    @BeforeEach
    void setUp() {
        solicitudRepository = org.mockito.Mockito.mock(SolicitudRepository.class);
        UsuarioRepository usuarioRepository = org.mockito.Mockito.mock(UsuarioRepository.class);
        solicitudMapper = org.mockito.Mockito.mock(SolicitudMapper.class);
        solicitudService = new SolicitudService(
                solicitudRepository, usuarioRepository, solicitudMapper);
    }

    @Test
    void pendingAppointmentCannotBeCompletedAndRemainsUnchanged() {
        Solicitud cita = citaPendiente();
        when(solicitudRepository.findByIdForUpdate(15L))
                .thenReturn(Optional.of(cita));
        SolicitudEstadoRequest request = new SolicitudEstadoRequest();
        request.setEstado(SolicitudEstado.COMPLETADA);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> solicitudService.cambiarEstado(15L, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(SolicitudEstado.PENDIENTE, cita.getEstado());
        verify(solicitudRepository).findByIdForUpdate(15L);
        verifyNoInteractions(solicitudMapper);
        org.mockito.Mockito.verify(solicitudRepository,
                org.mockito.Mockito.never()).saveAndFlush(cita);
    }

    @Test
    void clientLookupUsesOwnerAndHidesForeignFolio() {
        when(solicitudRepository.findByIdAndClienteId(99L, 8L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> solicitudService.obtenerPorId(99L, 8L, true));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(solicitudRepository).findByIdAndClienteId(99L, 8L);
        org.mockito.Mockito.verify(solicitudRepository,
                org.mockito.Mockito.never()).findById(99L);
    }

    @Test
    void appointmentMustBeConfirmedBeforeCompletion() {
        Solicitud cita = citaPendiente();
        SolicitudResponse response = SolicitudResponse.builder().id(15L).build();
        when(solicitudRepository.findByIdForUpdate(15L))
                .thenReturn(Optional.of(cita));
        when(solicitudRepository.saveAndFlush(cita)).thenReturn(cita);
        when(solicitudMapper.toResponse(cita)).thenReturn(response);

        SolicitudEstadoRequest confirmar = new SolicitudEstadoRequest();
        confirmar.setEstado(SolicitudEstado.CONFIRMADA);
        solicitudService.cambiarEstado(15L, confirmar);
        assertEquals(SolicitudEstado.CONFIRMADA, cita.getEstado());

        SolicitudEstadoRequest completar = new SolicitudEstadoRequest();
        completar.setEstado(SolicitudEstado.COMPLETADA);
        solicitudService.cambiarEstado(15L, completar);

        assertEquals(SolicitudEstado.COMPLETADA, cita.getEstado());
        verify(solicitudRepository, org.mockito.Mockito.times(2))
                .findByIdForUpdate(15L);
    }

    private Solicitud citaPendiente() {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(15L);
        solicitud.setCliente(Usuario.builder().id(8L).build());
        solicitud.setMascota("Luna");
        solicitud.setMotivo("Control anual");
        solicitud.setFechaHora(LocalDateTime.now().plusDays(5));
        solicitud.setEstado(SolicitudEstado.PENDIENTE);
        return solicitud;
    }
}
