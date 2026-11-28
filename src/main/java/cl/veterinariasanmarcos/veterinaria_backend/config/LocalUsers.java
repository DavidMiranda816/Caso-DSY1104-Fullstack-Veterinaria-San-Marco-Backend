package cl.veterinariasanmarcos.veterinaria_backend.config;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import cl.veterinariasanmarcos.veterinaria_backend.model.Usuario;
import cl.veterinariasanmarcos.veterinaria_backend.repository.UsuarioRepository;
@Configuration
@Profile("local")
public class LocalUsers {
 @Bean CommandLineRunner localSeed(UsuarioRepository repo, PasswordEncoder encoder) {
 return args -> {
 String[][] users={{"Admin Kaiju","admin@kaiju.com","admin123","ADMIN"},{"Ventas Kaiju","ventas@kaiju.com","ventas123","VENDEDOR"},{"Bodega Kaiju","bodega@kaiju.com","bodega123","ENCARGADO"}};
 for(String[] u:users) if(!repo.existsByCorreoIgnoreCase(u[1])) repo.save(Usuario.builder().nombre(u[0]).correo(u[1]).passwordHash(encoder.encode(u[2])).rol(u[3]).activo(true).fechaCreacion(LocalDateTime.now()).build());
 };
 }
}
