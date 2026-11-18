package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.model.Notificacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import co.edu.javeriana.talentmatch.model.Usuario;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.NotificacionRepository;
import co.edu.javeriana.talentmatch.repository.AnalistaRRHHRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notificaciones in-app. Se guardan en la misma transaccion que el evento que las origina,
 * asi quedan disponibles de inmediato sin depender de ninguna accion humana (RNF-08).
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificaciones;
    private final AnalistaRRHHRepository analistas;
    private final Clock reloj;

    public NotificacionService(NotificacionRepository notificaciones, AnalistaRRHHRepository analistas, Clock reloj) {
        this.notificaciones = notificaciones;
        this.analistas = analistas;
        this.reloj = reloj;
    }

    @Transactional
    public Notificacion notificar(Usuario destinatario, TipoNotificacion tipo, String titulo, String mensaje,
                                  Vacante vacante, Postulacion postulacion) {
        Notificacion n = new Notificacion(destinatario, tipo, titulo, mensaje, LocalDateTime.now(reloj));
        n.setVacante(vacante);
        n.setPostulacion(postulacion);
        return notificaciones.save(n);
    }

    /** Envia la misma notificacion a todos los analistas de RR.HH. activos. */
    @Transactional
    public void notificarRrhh(TipoNotificacion tipo, String titulo, String mensaje, Vacante vacante,
                              Postulacion postulacion) {
        analistas.findByActivoTrue().forEach(a -> notificar(a, tipo, titulo, mensaje, vacante, postulacion));
    }

    @Transactional(readOnly = true)
    public List<Notificacion> de(Long idUsuario) {
        return notificaciones.findByDestinatarioIdOrderByFechaDesc(idUsuario);
    }

    @Transactional
    public Notificacion marcarLeida(Long idUsuario, Long idNotificacion) {
        Notificacion n = notificaciones.findById(idNotificacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la notificación " + idNotificacion + "."));
        if (!n.getDestinatario().getId().equals(idUsuario)) {
            throw new AccesoDenegadoException("Esta notificación no es tuya.");
        }
        n.marcarLeida();
        return n;
    }
}
