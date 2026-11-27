package cl.veterinariasanmarcos.veterinaria_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.veterinariasanmarcos.veterinaria_backend.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
