package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.ReporteMensual;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link ReporteMensual}. Solo lee y guarda: no contiene reglas de negocio. */
public interface ReporteMensualRepository extends JpaRepository<ReporteMensual, Long> {

    boolean existsByPeriodo(String periodo);

    List<ReporteMensual> findAllByOrderByPeriodoDesc();
}
