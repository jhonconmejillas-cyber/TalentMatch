package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Vacante;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Vacante}. Solo lee y guarda: no contiene reglas de negocio. */
public interface VacanteRepository extends JpaRepository<Vacante, Long> {
}
