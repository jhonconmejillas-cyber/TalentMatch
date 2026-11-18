package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Postulacion}. Solo lee y guarda: no contiene reglas de negocio. */
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    List<Postulacion> findByEmpleadoIdOrderByFechaCreacionDesc(Long empleadoId);

    List<Postulacion> findByEmpleadoIdAndEstado(Long empleadoId, EstadoPostulacion estado);

    List<Postulacion> findByVacanteIdAndEstado(Long vacanteId, EstadoPostulacion estado);

    List<Postulacion> findByVacanteId(Long vacanteId);

    boolean existsByEmpleadoIdAndVacanteIdAndEstado(Long empleadoId, Long vacanteId, EstadoPostulacion estado);
}
