package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Manager}. Solo lee y guarda: no contiene reglas de negocio. */
public interface ManagerRepository extends JpaRepository<Manager, Long> {
}
