package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Postulacion}. Solo lee y guarda: no contiene reglas de negocio. */
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
}
