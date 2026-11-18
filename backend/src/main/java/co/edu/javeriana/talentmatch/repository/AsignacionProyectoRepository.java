package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link AsignacionProyecto}. Solo lee y guarda: no contiene reglas de negocio. */
public interface AsignacionProyectoRepository extends JpaRepository<AsignacionProyecto, Long> {

    List<AsignacionProyecto> findByFechaFinIsNullOrderByProyectoNombreAsc();

    Optional<AsignacionProyecto> findByEmpleadoIdAndFechaFinIsNull(Long empleadoId);

    List<AsignacionProyecto> findByEmpleadoIdOrderByFechaInicioDesc(Long empleadoId);
}
