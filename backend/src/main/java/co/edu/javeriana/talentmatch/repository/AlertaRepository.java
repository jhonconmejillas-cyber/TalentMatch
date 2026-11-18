package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Alerta}. Solo lee y guarda: no contiene reglas de negocio. */
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
}
