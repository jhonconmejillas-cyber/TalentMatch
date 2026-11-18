package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Alerta;
import co.edu.javeriana.talentmatch.model.EstadoAlerta;
import co.edu.javeriana.talentmatch.model.TipoAlerta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Alerta}. Solo lee y guarda: no contiene reglas de negocio. */
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    boolean existsByTipoAndVacanteId(TipoAlerta tipo, Long vacanteId);

    List<Alerta> findAllByOrderByFechaDesc();

    List<Alerta> findByEstadoOrderByFechaDesc(EstadoAlerta estado);

    List<Alerta> findByTipoAndManagerId(TipoAlerta tipo, Long managerId);
}
