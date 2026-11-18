package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.model.Alerta;
import co.edu.javeriana.talentmatch.model.AnalistaRRHH;
import co.edu.javeriana.talentmatch.model.EstadoAlerta;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.TipoAlerta;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import co.edu.javeriana.talentmatch.repository.AlertaRepository;
import co.edu.javeriana.talentmatch.repository.AnalistaRRHHRepository;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Control automatico ante un patron de rechazo (RN-29, CU_24, HU-004 escenario 3).
 *
 * Cada vez que un manager rechaza DIRECTAMENTE a un candidato viable (>= umbral) se cuentan sus
 * rechazos viables posteriores a su ultima alerta; al llegar al limite configurado
 * (alertas.rechazos_viables_para_alerta, 2) se crea una alerta para RR.HH. con los puntajes
 * comparados. Ningun empleado tiene que reclamar.
 */
@Service
public class AlertaService implements DecisionService.EventosDecision {

    /** Antes de cualquier dato del sistema: "desde siempre" para el primer conteo. */
    private static final LocalDateTime INICIO = LocalDateTime.of(2000, 1, 1, 0, 0);

    private final AlertaRepository alertas;
    private final PostulacionRepository postulaciones;
    private final AnalistaRRHHRepository analistas;
    private final NotificacionService notificaciones;
    private final ParametrosService parametros;
    private final Clock reloj;

    public AlertaService(AlertaRepository alertas, PostulacionRepository postulaciones,
                         AnalistaRRHHRepository analistas, NotificacionService notificaciones,
                         ParametrosService parametros, Clock reloj) {
        this.alertas = alertas;
        this.postulaciones = postulaciones;
        this.analistas = analistas;
        this.notificaciones = notificaciones;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    @Override
    public void alRechazar(Postulacion rechazada, boolean porDecisionDirecta, LocalDateTime ahora) {
        double umbral = parametros.umbralViabilidad();
        if (!porDecisionDirecta || !rechazada.esViable(umbral)) {
            return;
        }
        Manager manager = rechazada.getVacante().getManager();
        LocalDateTime desde = alertas.findByTipoAndManagerId(TipoAlerta.RECHAZOS_REPETIDOS, manager.getId()).stream()
                .map(Alerta::getFecha).max(Comparator.naturalOrder()).orElse(INICIO);

        List<Postulacion> rechazos = postulaciones
                .findByVacanteManagerIdAndRechazoDirectoTrueAndPuntajeFinalGreaterThanEqualAndFechaResolucionAfterOrderByFechaResolucionAsc(
                        manager.getId(), umbral, desde);
        if (rechazos.size() < parametros.rechazosViablesParaAlerta()) {
            return;
        }
        String detalle = "El manager " + manager.getNombre() + " (ID " + manager.getId() + ") rechazó "
                + rechazos.size() + " candidatos viables (Puntaje Final ≥ " + String.format(Locale.ROOT, "%.0f", umbral)
                + " %): " + rechazos.stream().map(p -> String.format(Locale.ROOT, "%s con %.1f %% en \"%s\"",
                        RankingService.alias(p.getEmpleado()), p.getPuntajeFinal(), p.getVacante().getTitulo()))
                .collect(Collectors.joining("; ")) + ".";
        alertas.save(new Alerta(TipoAlerta.RECHAZOS_REPETIDOS, manager, rechazada.getVacante(), detalle, ahora));
        notificaciones.notificarRrhh(TipoNotificacion.ALERTA, "Alerta: rechazos repetidos de candidatos viables",
                detalle, rechazada.getVacante(), null);
    }

    @Transactional(readOnly = true)
    public List<Alerta> listar(EstadoAlerta estado) {
        return estado == null ? alertas.findAllByOrderByFechaDesc() : alertas.findByEstadoOrderByFechaDesc(estado);
    }

    @Transactional
    public Alerta resolver(Long idAnalista, Long idAlerta) {
        Alerta alerta = alertas.findById(idAlerta)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alerta " + idAlerta + "."));
        AnalistaRRHH analista = analistas.findById(idAnalista)
                .orElseThrow(() -> new AccesoDenegadoException("Solo RR.HH. puede resolver alertas."));
        analista.revisarAlerta(alerta, LocalDateTime.now(reloj));
        return alerta;
    }
}
