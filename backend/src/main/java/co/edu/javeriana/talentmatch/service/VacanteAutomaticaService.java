package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.model.Alerta;
import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.EstadoVacante;
import co.edu.javeriana.talentmatch.model.Habilidad;
import co.edu.javeriana.talentmatch.model.Manager;
import co.edu.javeriana.talentmatch.model.MotivoFinAsignacion;
import co.edu.javeriana.talentmatch.model.Senioridad;
import co.edu.javeriana.talentmatch.model.TipoAlerta;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import co.edu.javeriana.talentmatch.model.Vacante;
import co.edu.javeriana.talentmatch.repository.AlertaRepository;
import co.edu.javeriana.talentmatch.repository.AsignacionProyectoRepository;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import co.edu.javeriana.talentmatch.repository.VacanteRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ciclo automatico de las vacantes (HU-009, CU_18, CU_19):
 * - RN-02: al liberarse una asignacion se crea la vacante del puesto.
 * - RN-03: si el origen es una rotacion aceptada, se activa 24 h despues; si no, de inmediato.
 * - RN-04: cada eslabon de una cadena de rotaciones es una vacante independiente.
 * - RN-05 / RN-30: una vacante activa sin match mas alla del umbral alerta al manager y al jefe del area.
 * - RF-10: al activarse, avisa a quienes tienen sus habilidades en la wishlist.
 */
@Service
public class VacanteAutomaticaService {

    private static final Logger LOG = LoggerFactory.getLogger(VacanteAutomaticaService.class);

    private final VacanteRepository vacantes;
    private final AsignacionProyectoRepository asignaciones;
    private final AlertaRepository alertas;
    private final EmpleadoRepository empleados;
    private final NotificacionService notificaciones;
    private final ParametrosService parametros;

    public VacanteAutomaticaService(VacanteRepository vacantes, AsignacionProyectoRepository asignaciones,
                                    AlertaRepository alertas, EmpleadoRepository empleados,
                                    NotificacionService notificaciones, ParametrosService parametros) {
        this.vacantes = vacantes;
        this.asignaciones = asignaciones;
        this.alertas = alertas;
        this.empleados = empleados;
        this.notificaciones = notificaciones;
        this.parametros = parametros;
    }

    /**
     * RN-02: el empleado deja el puesto y nace la vacante. Hereda el cargo (titulo), el area del
     * proyecto, la senioridad y, como habilidades criticas, las habilidades avaladas de quien sale.
     * El manager solicitante es el manager actual de quien sale.
     */
    @Transactional
    public Vacante liberarAsignacion(AsignacionProyecto asignacion, MotivoFinAsignacion motivo, LocalDateTime ahora) {
        asignacion.liberar(motivo, ahora.toLocalDate());
        // Se escribe ya: si el empleado recibe otra asignacion en la misma transaccion, la base
        // debe ver esta cerrada (maximo una asignacion vigente por empleado).
        asignaciones.saveAndFlush(asignacion);

        Empleado saliente = asignacion.getEmpleado();
        Senioridad senioridad = saliente.getSenioridad() == null ? Senioridad.SEMI_SENIOR : saliente.getSenioridad();
        Vacante vacante = new Vacante(asignacion.getCargo(), senioridad, asignacion.getProyecto().getArea(),
                saliente.getManagerActual(), ahora);
        vacante.setProyecto(asignacion.getProyecto());
        vacante.setOrigen(motivo.origenVacante());
        vacante.setDescripcion("Vacante generada automaticamente al liberarse el puesto de "
                + asignacion.getCargo() + " en " + asignacion.getProyecto().getNombre() + ".");
        vacante.setHabilidadesCriticas(new HashSet<>(saliente.habilidadesAvaladas()));

        if (vacante.getOrigen().requiereActivacionDiferida()) {
            // RN-03: ventana de revision antes de ser visible
            vacante.programarActivacion(ahora.plusHours(parametros.activacionDiferidaHoras()));
            vacantes.save(vacante);
        } else {
            vacante.activar(ahora);
            vacantes.save(vacante);
            avisarWishlist(vacante);
        }
        return vacante;
    }

    /** RN-03: activa las vacantes cuya ventana de 24 h ya paso. Devuelve cuantas activo. */
    @Transactional
    public int activarPendientes(LocalDateTime ahora) {
        List<Vacante> listas = vacantes.findByEstadoOrderByFechaActivacionDesc(EstadoVacante.PENDIENTE_ACTIVACION)
                .stream().filter(v -> v.debeActivarse(ahora)).toList();
        for (Vacante v : listas) {
            v.activar(ahora);
            avisarWishlist(v);
        }
        if (!listas.isEmpty()) {
            LOG.info("Vacantes activadas tras la ventana de revision: {}", listas.size());
        }
        return listas.size();
    }

    /**
     * RN-05 / RN-30: alerta una sola vez por cada vacante ACTIVA que lleva mas dias que el umbral sin match.
     * Notifica al manager solicitante y al jefe del area (el manager de ese manager; si no tiene, RR.HH.).
     */
    @Transactional
    public int revisarVacantesSinMatch(LocalDateTime ahora) {
        int dias = parametros.umbralVacanteSinMatchDias();
        int nuevas = 0;
        for (Vacante v : vacantes.findByEstadoOrderByFechaActivacionDesc(EstadoVacante.ACTIVA)) {
            boolean vencida = v.getFechaActivacion() != null && v.getFechaActivacion().plusDays(dias).isBefore(ahora);
            if (!vencida || alertas.existsByTipoAndVacanteId(TipoAlerta.VACANTE_SIN_MATCH, v.getId())) {
                continue;
            }
            String detalle = "La vacante \"" + v.getTitulo() + "\" (" + v.getArea() + ") lleva más de " + dias
                    + " días activa sin match.";
            alertas.save(new Alerta(TipoAlerta.VACANTE_SIN_MATCH, v.getManager(), v, detalle, ahora));
            String titulo = "Vacante sin match: " + v.getTitulo();
            String mensaje = detalle + " Decide si se contrata externamente o se elimina el puesto (fuera de la app).";
            Manager solicitante = v.getManager();
            if (solicitante != null) {
                notificaciones.notificar(solicitante, TipoNotificacion.VACANTE_SIN_MATCH, titulo, mensaje, v, null);
            }
            Manager jefe = solicitante == null ? null : solicitante.getManagerActual();
            if (jefe != null) {
                notificaciones.notificar(jefe, TipoNotificacion.VACANTE_SIN_MATCH, titulo, mensaje, v, null);
            } else {
                notificaciones.notificarRrhh(TipoNotificacion.VACANTE_SIN_MATCH, titulo, mensaje, v, null);
            }
            nuevas++;
        }
        return nuevas;
    }

    /** RF-10 / CU_08: aviso proactivo a quien tiene en su wishlist alguna habilidad de la vacante. */
    private void avisarWishlist(Vacante vacante) {
        Set<Long> ids = new HashSet<>();
        vacante.habilidadesRequeridas().stream().map(Habilidad::getId).forEach(ids::add);
        if (ids.isEmpty()) {
            return;
        }
        for (Empleado e : empleados.conAlgunaEnWishlist(ids)) {
            notificaciones.notificar(e, TipoNotificacion.VACANTE_WISHLIST,
                    "Nueva vacante con habilidades de tu wishlist",
                    "Se abrió la vacante \"" + vacante.getTitulo() + "\" (" + vacante.getArea()
                            + "), que incluye habilidades que quieres aprender.", vacante, null);
        }
    }
}
