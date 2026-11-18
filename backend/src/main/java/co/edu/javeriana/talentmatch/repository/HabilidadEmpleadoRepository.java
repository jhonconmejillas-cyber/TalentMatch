package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.EstadoHabilidad;
import co.edu.javeriana.talentmatch.model.HabilidadEmpleado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link HabilidadEmpleado}. Solo lee y guarda: no contiene reglas de negocio. */
public interface HabilidadEmpleadoRepository extends JpaRepository<HabilidadEmpleado, Long> {

    List<HabilidadEmpleado> findByEmpleadoIdOrderByFechaAgregadoDesc(Long empleadoId);

    /** Habilidades de los empleados cuyo manager actual es {@code managerId}, en un estado. */
    List<HabilidadEmpleado> findByEmpleadoManagerActualIdAndEstadoOrderByFechaAgregadoAsc(
            Long managerId, EstadoHabilidad estado);

    List<HabilidadEmpleado> findByEmpleadoManagerActualIdOrderByFechaAgregadoDesc(Long managerId);
}
