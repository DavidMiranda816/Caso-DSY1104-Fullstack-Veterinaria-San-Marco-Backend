package cl.veterinariasanmarcos.veterinaria_backend.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.veterinariasanmarcos.veterinaria_backend.controller.CategoriaController;
import cl.veterinariasanmarcos.veterinaria_backend.controller.ProductoController;
import cl.veterinariasanmarcos.veterinaria_backend.dto.ProductoResponse;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;
import cl.veterinariasanmarcos.veterinaria_backend.services.CategoriaService;
import cl.veterinariasanmarcos.veterinaria_backend.services.ProductoService;

@WebMvcTest(controllers = {CategoriaController.class, ProductoController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=test-issuer",
        "app.jwt.duration-seconds=3600",
        "app.cors.allowed-origins=http://localhost"
})
class CatalogAuthorizationHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void catalogReadsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedClientCanReadCatalog() throws Exception {
        when(usuarioRepository.findById(8L))
                .thenReturn(Optional.of(Usuario.builder()
                        .id(8L)
                        .nombre("Cliente")
                        .rol("CLIENTE")
                        .activo(true)
                        .build()));
        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(Usuario.builder()
                        .id(10L)
                        .nombre("Recepcionista")
                        .rol("RECEPCIONISTA")
                        .activo(true)
                        .build()));
        when(productoService.listar()).thenReturn(List.of());
        when(categoriaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer " + token("8", "CLIENTE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/productos")
                        .header("Authorization", "Bearer " + token("8", "CLIENTE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/categorias")
                        .header("Authorization", "Bearer " + token("8", "CLIENTE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/categorias")
                        .header("Authorization", "Bearer " + token("8", "CLIENTE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/productos")
                        .header("Authorization", "Bearer " + token("10", "RECEPCIONISTA")))
                .andExpect(status().isOk());
    }

    @Test
    void productCreationRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotCreateCategory() throws Exception {
        when(usuarioRepository.findById(8L))
                .thenReturn(Optional.of(Usuario.builder()
                        .id(8L)
                        .nombre("CLIENTE")
                        .rol("CLIENTE")
                        .activo(true)
                        .build()));

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token("8", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Alimento",
                                  "descripcion": "Alimento para mascotas"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        when(usuarioRepository.findById(9L))
                .thenReturn(Optional.of(Usuario.builder()
                        .id(9L)
                        .rol("ADMIN")
                        .activo(true)
                        .build()));
        when(productoService.crear(any()))
                .thenReturn(ProductoResponse.builder()
                        .id(3L)
                        .nombre("Alimento premium")
                        .categoria("Alimento")
                        .precio(new BigDecimal("24990"))
                        .stock(10)
                        .build());

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token("9", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + token("9", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson()))
                .andExpect(status().isCreated());
    }

    private String productJson() {
        return """
                {
                  "nombre": "Alimento premium",
                  "categoria": "Alimento",
                  "precio": 24990,
                  "stock": 10
                }
                """;
    }

    private String token(String subject, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("test-issuer")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .claim("roles", List.of(role))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims)).getTokenValue();
    }
}
