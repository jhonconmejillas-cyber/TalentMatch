package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Usuario}. Solo lee y guarda: no contiene reglas de negocio. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}
