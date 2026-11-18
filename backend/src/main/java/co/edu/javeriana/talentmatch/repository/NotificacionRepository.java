package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Notificacion}. Solo lee y guarda: no contiene reglas de negocio. */
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
}
