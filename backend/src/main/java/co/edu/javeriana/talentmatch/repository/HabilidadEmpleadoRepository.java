package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.HabilidadEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link HabilidadEmpleado}. Solo lee y guarda: no contiene reglas de negocio. */
public interface HabilidadEmpleadoRepository extends JpaRepository<HabilidadEmpleado, Long> {
}
