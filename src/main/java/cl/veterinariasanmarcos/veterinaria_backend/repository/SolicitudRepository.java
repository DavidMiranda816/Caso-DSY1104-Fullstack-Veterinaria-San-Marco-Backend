package cl.veterinariasanmarcos.veterinaria_backend.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.veterinariasanmarcos.veterinaria_backend.model.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByFechaHoraAsc();

    List<Solicitud> findAllByClienteIdOrderByFechaHoraAsc(Long clienteId);

    Optional<Solicitud> findByIdAndClienteId(Long id, Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Solicitud s where s.id = :id")
    Optional<Solicitud> findByIdForUpdate(@Param("id") Long id);
}
