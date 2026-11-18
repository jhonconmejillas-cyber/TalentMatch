package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link AsignacionProyecto}. Solo lee y guarda: no contiene reglas de negocio. */
public interface AsignacionProyectoRepository extends JpaRepository<AsignacionProyecto, Long> {
}
