package cl.veterinariasanmarcos.veterinaria_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
} 