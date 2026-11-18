package co.edu.javeriana.talentmatch.repository;

import co.edu.javeriana.talentmatch.model.Notificacion;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Notificacion}. Solo lee y guarda: no contiene reglas de negocio. */
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByDestinatarioIdOrderByFechaDesc(Long destinatarioId);

    List<Notificacion> findByTipo(TipoNotificacion tipo);

    long countByDestinatarioIdAndLeidaFalse(Long destinatarioId);
}
