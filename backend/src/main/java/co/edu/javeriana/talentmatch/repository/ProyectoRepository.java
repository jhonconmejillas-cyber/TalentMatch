package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Proyecto}. Solo lee y guarda: no contiene reglas de negocio. */
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
}
