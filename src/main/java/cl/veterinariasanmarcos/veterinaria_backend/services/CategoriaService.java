package cl.veterinariasanmarcos.veterinaria_backend.services;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.veterinariasanmarcos.veterinaria_backend.dto.CategoriaRequest;
import cl.veterinariasanmarcos.veterinaria_backend.dto.CategoriaResponse;
import cl.veterinariasanmarcos.veterinaria_backend.mapper.CategoriaMapper;
import cl.veterinariasanmarcos.veterinaria_backend.model.Categoria;
import cl.veterinariasanmarcos.veterinaria_backend.repository.CategoriaRepository;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        return categoriaMapper.toResponse(buscar(id));
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = normalizarNombre(request.getNombre());
        validarNombreDisponible(nombre, null);

        Categoria categoria = categoriaMapper.fromRequest(request);
        categoria.setNombre(nombre);
        normalizarDescripcion(categoria);

        return categoriaMapper.toResponse(categoriaRepository.saveAndFlush(categoria));
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        String nombre = normalizarNombre(request.getNombre());
        validarNombreDisponible(nombre, id);

        categoria.setNombre(nombre);
        categoria.setDescripcion(request.getDescripcion());
        normalizarDescripcion(categoria);

        return categoriaMapper.toResponse(categoriaRepository.saveAndFlush(categoria));
    }

    public void eliminar(Long id) {
        categoriaRepository.delete(buscar(id));
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada"));
    }

    private void validarNombreDisponible(String nombre, Long id) {
        boolean existe = id == null
                ? categoriaRepository.existsByNombreIgnoreCase(nombre)
                : categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (existe) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre de categoría ya está registrado");
        }
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim();
    }

    private void normalizarDescripcion(Categoria categoria) {
        if (categoria.getDescripcion() != null) {
            categoria.setDescripcion(categoria.getDescripcion().trim());
        }
    }
}
