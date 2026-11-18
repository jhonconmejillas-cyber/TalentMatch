package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.dto.RankingDtos.DecisionDto;
import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.model.MotivoFinAsignacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.ReglaNegocioException;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.ManagerRepository;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decision del manager sobre el ranking (CU_15, CU_16):
 * - RN-13 / RN-17: acepta o rechaza sin redactar justificacion; nada bloquea el cierre.
 * - RN-12: solo sobre el lote de peticiones del ciclo ya cumplido.
 * - Al aceptar: cierra la vacante, registra la posicion de cada candidato (HU-004), rechaza a los
 *   demas candidatos de la vacante, cancela las otras postulaciones del aceptado (RN-09),
 *   mueve su asignacion sin aprobacion de su manager actual (RN-15), lo que libera su puesto
 *   y genera la vacante con activacion diferida (RN-02, RN-03), y notifica SOLO a RR.HH. (RN-16).
 */
@Service
public class DecisionService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'a las' HH:mm",
            Locale.forLanguageTag("es-CO"));

    private final PostulacionRepository postulaciones;
    private final VacanteRepository vacantes;
    private final ManagerRepository managers;
    private final RankingService ranking;
    private final PostulacionService postulacionService;
    private final VacanteAutomaticaService vacantesAutomaticas;
    private final NotificacionService notificaciones;
    private final ParametrosService parametros;
    private final List<EventosDecision> oyentes;
    private final Clock reloj;

    public DecisionService(PostulacionRepository postulaciones, VacanteRepository vacantes, ManagerRepository managers,
                           RankingService ranking, PostulacionService postulacionService,
                           VacanteAutomaticaService vacantesAutomaticas, NotificacionService notificaciones,
                           ParametrosService parametros, List<EventosDecision> oyentes, Clock reloj) {
        this.postulaciones = postulaciones;
        this.vacantes = vacantes;
        this.managers = managers;
        this.ranking = ranking;
        this.postulacionService = postulacionService;
        this.vacantesAutomaticas = vacantesAutomaticas;
        this.notificaciones = notificaciones;
        this.parametros = parametros;
        this.oyentes = oyentes;
        this.reloj = reloj;
    }

    /** Puntos de extension: otras reglas reaccionan a las decisiones (alertas RN-29, resultado RN-18). */
    public interface EventosDecision {

        default void alRechazar(Postulacion rechazada, boolean porDecisionDirecta, LocalDateTime ahora) {
        }

        default void alCerrarVacante(Vacante vacante, Postulacion aceptada, List<Postulacion> rechazadas,
                                     LocalDateTime ahora) {
        }
    }

    @Transactional
    public DecisionDto aceptar(Long idManager, Long idPostulacion) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Postulacion aceptada = postulacionDecidible(idManager, idPostulacion, ahora);
        Vacante vacante = aceptada.getVacante();
        Manager manager = vacante.getManager();

        manager.aceptarCandidato(aceptada, ahora);

        // HU-004: posicion de cada candidato al momento de la decision ("Fuiste #1 de 12")
        List<Postulacion> orden = ranking.ordenCompleto(vacante.getId());
        for (int i = 0; i < orden.size(); i++) {
            orden.get(i).registrarPosicion(i + 1, orden.size());
        }
        // Los demas candidatos de esta vacante quedan no seleccionados: el cierre no espera a nadie (RN-17)
        List<Postulacion> noSeleccionados = orden.stream().filter(Postulacion::estaActiva).toList();
        noSeleccionados.forEach(p -> p.rechazar(ahora));
        vacante.cerrar(ahora);
        postulaciones.saveAll(orden);
        vacantes.save(vacante);

        postulacionService.cancelarOtrasPostulaciones(aceptada, ahora); // RN-09, sin notificar
        moverAsignacion(aceptada.getEmpleado(), vacante, ahora);         // RN-15

        // RN-16: solo RR.HH. recibe la notificacion de la app
        notificaciones.notificarRrhh(TipoNotificacion.ROTACION_ACEPTADA, "Rotación aceptada: " + vacante.getTitulo(),
                aceptada.getEmpleado().getNombre() + " fue aceptado(a) en la vacante \"" + vacante.getTitulo()
                        + "\" (" + vacante.getArea() + "). Revisa la aceptación y avisa a las partes involucradas.",
                vacante, aceptada);

        oyentes.forEach(o -> {
            noSeleccionados.forEach(p -> o.alRechazar(p, false, ahora));
            o.alCerrarVacante(vacante, aceptada, noSeleccionados, ahora);
        });
        return new DecisionDto(aceptada.getId(), aceptada.getEstado(),
                "Candidato aceptado. La vacante se cerró y RR.HH. fue notificado.");
    }

    /** RN-17: rechazar no exige texto ni bloquea nada. */
    @Transactional
    public DecisionDto rechazar(Long idManager, Long idPostulacion) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Postulacion postulacion = postulacionDecidible(idManager, idPostulacion, ahora);
        postulacion.getVacante().getManager().rechazarCandidato(postulacion, ahora);
        postulaciones.save(postulacion);
        oyentes.forEach(o -> o.alRechazar(postulacion, true, ahora));
        return new DecisionDto(postulacion.getId(), postulacion.getEstado(), "Candidato rechazado.");
    }

    private Postulacion postulacionDecidible(Long idManager, Long idPostulacion, LocalDateTime ahora) {
        Postulacion p = postulaciones.findById(idPostulacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la postulación " + idPostulacion + "."));
        Manager manager = managers.findById(idManager)
                .orElseThrow(() -> new AccesoDenegadoException("Solo un manager puede decidir sobre una postulación."));
        Vacante vacante = p.getVacante();
        if (!manager.mismoUsuarioQue(vacante.getManager())) {
            throw new AccesoDenegadoException("Solo el manager que publicó la vacante puede decidir sobre ella.");
        }
        if (!vacante.estaActiva()) {
            throw new ReglaNegocioException("La vacante ya no está activa.");
        }
        if (!p.estaActiva()) {
            throw new ReglaNegocioException("La postulación ya fue resuelta.");
        }
        if (!p.esViable(parametros.umbralViabilidad())) {
            throw new ReglaNegocioException("El candidato no está en el ranking: su Puntaje Final es menor al umbral.");
        }
        LocalDateTime corte = ranking.corteCiclo(vacante, ahora);
        if (corte == null || !p.getFechaCreacion().isBefore(corte)) {
            // Sin corte aun: el primer ciclo cuenta desde la activacion (o desde ahora si no hay fecha)
            LocalDateTime base = corte != null ? corte
                    : Objects.requireNonNullElse(vacante.getFechaActivacion(), ahora);
            LocalDateTime proximo = base.plusDays(parametros.cicloEvaluacionDias());
            throw new ReglaNegocioException("Esta petición se resuelve en el próximo ciclo de evaluación, a partir del "
                    + FECHA.format(proximo) + ".");
        }
        return p;
    }

    /** RN-15: el cambio de asignacion es automatico, sin aprobacion del manager actual. */
    private void moverAsignacion(Empleado empleado, Vacante vacante, LocalDateTime ahora) {
        empleado.asignacionActual().ifPresent(actual ->
                vacantesAutomaticas.liberarAsignacion(actual, MotivoFinAsignacion.ROTACION_ACEPTADA, ahora));
        if (vacante.getProyecto() != null) {
            empleado.agregarAsignacion(new AsignacionProyecto(empleado, vacante.getProyecto(), vacante.getTitulo(),
                    ahora.toLocalDate()));
        }
        empleado.setFechaInicioRol(ahora.toLocalDate()); // empieza a contar la antiguedad del nuevo rol (RN-07)
        empleado.setArea(vacante.getArea());
        if (vacante.getManager() != null && !vacante.getManager().mismoUsuarioQue(empleado)) {
            empleado.setManagerActual(vacante.getManager());
        }
    }
}
