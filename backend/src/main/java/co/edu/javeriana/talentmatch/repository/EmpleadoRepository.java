package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Empleado}. Solo lee y guarda: no contiene reglas de negocio. */
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}
