package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Habilidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Habilidad}. Solo lee y guarda: no contiene reglas de negocio. */
public interface HabilidadRepository extends JpaRepository<Habilidad, Long> {

    Optional<Habilidad> findByNombreIgnoreCase(String nombre);

    List<Habilidad> findAllByOrderByNombreAsc();
}
