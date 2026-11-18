package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.EstadoVacante;
import co.edu.javeriana.talentmatch.model.Vacante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Vacante}. Solo lee y guarda: no contiene reglas de negocio. */
public interface VacanteRepository extends JpaRepository<Vacante, Long> {

    List<Vacante> findByEstadoOrderByFechaActivacionDesc(EstadoVacante estado);

    List<Vacante> findByManagerIdOrderByFechaCreacionDesc(Long managerId);
}
