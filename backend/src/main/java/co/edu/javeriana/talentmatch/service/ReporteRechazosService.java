package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.config.ReporteRechazosProperties;
import co.edu.javeriana.talentmatch.model.Alerta;
import co.edu.javeriana.talentmatch.model.EstadoPostulacion;
import co.edu.javeriana.talentmatch.model.Postulacion;
import co.edu.javeriana.talentmatch.model.ReporteMensual;
import co.edu.javeriana.talentmatch.model.TipoAlerta;
import co.edu.javeriana.talentmatch.model.TipoNotificacion;
import co.edu.javeriana.talentmatch.repository.AlertaRepository;
import co.edu.javeriana.talentmatch.repository.PostulacionRepository;
import co.edu.javeriana.talentmatch.repository.ReporteMensualRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reporte mensual de patrones de rechazo para RR.HH. (HU-002, complementa la alerta de RN-29).
 *
 * - Por manager: tasa de aceptacion/rechazo y tasa de rechazo a candidatos viables (>= umbral).
 * - Se resalta a quien supera el promedio de la organizacion en mas de N puntos porcentuales.
 * - Anonimizado: los empleados aparecen como Empleado_007; los managers con su ID.
 * - Las alertas de RN-29 del periodo quedan enlazadas a la fila del manager.
 * No senala culpables: solo describe patrones ("X sucedio, X veces").
 */
@Service
public class ReporteRechazosService {

    private final PostulacionRepository postulaciones;
    private final AlertaRepository alertas;
    private final ReporteMensualRepository reportes;
    private final NotificacionService notificaciones;
    private final ParametrosService parametros;
    private final ReporteRechazosProperties config;
    private final Clock reloj;

    public ReporteRechazosService(PostulacionRepository postulaciones, AlertaRepository alertas,
                                  ReporteMensualRepository reportes, NotificacionService notificaciones,
                                  ParametrosService parametros, ReporteRechazosProperties config, Clock reloj) {
        this.postulaciones = postulaciones;
        this.alertas = alertas;
        this.reportes = reportes;
        this.notificaciones = notificaciones;
        this.parametros = parametros;
        this.config = config;
        this.reloj = reloj;
    }

    public record RechazoDetalle(String candidato, double puntajeFinal, Double puntajeSeleccionado, String vacante,
                                 LocalDateTime fecha) {
    }

    public record FilaManager(Long managerId, int decisiones, int aceptadas, int rechazadas, double tasaAceptacion,
                              double tasaRechazo, int candidatosViables, int rechazosViables,
                              double tasaRechazoViables, double desviacion, boolean resaltado,
                              List<Long> alertasRelacionadas, List<RechazoDetalle> detalle) {
    }

    public record Reporte(String periodo, LocalDateTime desde, LocalDateTime hasta, double umbralViabilidad,
                          double promedioTasaRechazoViables, int desviacionResaltadoPct, List<FilaManager> managers) {
    }

    /** Proceso nocturno del dia 1: genera el reporte del mes anterior una sola vez y avisa a RR.HH. */
    @Transactional
    public Optional<ReporteMensual> generarMesAnterior() {
        YearMonth periodo = YearMonth.now(reloj).minusMonths(1);
        if (reportes.existsByPeriodo(periodo.toString())) {
            return Optional.empty();
        }
        Reporte r = calcular(periodo);
        int anomalias = (int) r.managers().stream().filter(FilaManager::resaltado).count();
        ReporteMensual registro = reportes.save(new ReporteMensual(periodo.toString(), LocalDateTime.now(reloj),
                r.managers().size(), anomalias));
        notificaciones.notificarRrhh(TipoNotificacion.ALERTA, "Reporte mensual de rechazos " + periodo,
                "Ya está disponible el reporte de " + periodo + ": " + r.managers().size() + " managers analizados, "
                        + anomalias + " con desviación mayor a " + config.desviacionResaltadoPct() + " puntos.",
                null, null);
        return Optional.of(registro);
    }

    @Transactional(readOnly = true)
    public List<ReporteMensual> generados() {
        return reportes.findAllByOrderByPeriodoDesc();
    }

    /** Calcula el reporte de la ventana que termina en {@code periodo} (inclusive). */
    @Transactional(readOnly = true)
    public Reporte calcular(YearMonth periodo) {
        LocalDateTime hasta = periodo.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime desde = periodo.minusMonths(config.mesesVentana() - 1L).atDay(1).atStartOfDay();
        double umbral = parametros.umbralViabilidad();

        List<Postulacion> resueltas = postulaciones.findByEstadoInAndFechaResolucionGreaterThanEqualAndFechaResolucionLessThan(
                List.of(EstadoPostulacion.ACEPTADA, EstadoPostulacion.RECHAZADA), desde, hasta);
        Map<Long, List<Postulacion>> porManager = resueltas.stream()
                .filter(p -> p.getVacante().getManager() != null)
                .collect(Collectors.groupingBy(p -> p.getVacante().getManager().getId(), LinkedHashMap::new,
                        Collectors.toList()));

        List<FilaManager> filas = new ArrayList<>();
        for (Map.Entry<Long, List<Postulacion>> e : porManager.entrySet()) {
            filas.add(fila(e.getKey(), e.getValue(), umbral, desde, hasta));
        }
        double promedio = filas.stream().filter(f -> f.candidatosViables() > 0)
                .mapToDouble(FilaManager::tasaRechazoViables).average().orElse(0);
        List<FilaManager> conDesviacion = filas.stream().map(f -> {
            double desviacion = redondear(f.tasaRechazoViables() - promedio);
            return new FilaManager(f.managerId(), f.decisiones(), f.aceptadas(), f.rechazadas(), f.tasaAceptacion(),
                    f.tasaRechazo(), f.candidatosViables(), f.rechazosViables(), f.tasaRechazoViables(), desviacion,
                    f.candidatosViables() > 0 && desviacion > config.desviacionResaltadoPct(),
                    f.alertasRelacionadas(), f.detalle());
        }).sorted(Comparator.comparingDouble(FilaManager::tasaRechazoViables).reversed()
                .thenComparing(FilaManager::managerId)).toList();

        return new Reporte(periodo.toString(), desde, hasta, umbral, redondear(promedio),
                config.desviacionResaltadoPct(), conDesviacion);
    }

    private FilaManager fila(Long managerId, List<Postulacion> lista, double umbral, LocalDateTime desde,
                             LocalDateTime hasta) {
        int aceptadas = (int) lista.stream().filter(p -> p.getEstado() == EstadoPostulacion.ACEPTADA).count();
        int rechazadas = lista.size() - aceptadas;
        List<Postulacion> viables = lista.stream().filter(p -> p.esViable(umbral)).toList();
        List<Postulacion> rechazosViables = viables.stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.RECHAZADA).toList();

        List<RechazoDetalle> detalle = rechazosViables.stream()
                .sorted(Comparator.comparing(Postulacion::getFechaResolucion))
                .map(p -> new RechazoDetalle(RankingService.alias(p.getEmpleado()), p.getPuntajeFinal(),
                        puntajeSeleccionado(p), p.getVacante().getTitulo(), p.getFechaResolucion()))
                .toList();
        List<Long> alertasManager = alertas.findByTipoAndManagerId(TipoAlerta.RECHAZOS_REPETIDOS, managerId).stream()
                .filter(a -> !a.getFecha().isBefore(desde) && a.getFecha().isBefore(hasta))
                .map(Alerta::getId).toList();

        return new FilaManager(managerId, lista.size(), aceptadas, rechazadas, porcentaje(aceptadas, lista.size()),
                porcentaje(rechazadas, lista.size()), viables.size(), rechazosViables.size(),
                porcentaje(rechazosViables.size(), viables.size()), 0, false, alertasManager, detalle);
    }

    private Double puntajeSeleccionado(Postulacion rechazada) {
        return postulaciones.findByVacanteIdAndEstado(rechazada.getVacante().getId(), EstadoPostulacion.ACEPTADA)
                .stream().findFirst().map(Postulacion::getPuntajeFinal).orElse(null);
    }

    /** Archivo descargable (CSV, se abre en Excel). La columna "Resaltado" marca las desviaciones. */
    public String csv(Reporte r) {
        StringBuilder sb = new StringBuilder("﻿"); // BOM para que Excel lea las tildes
        sb.append("Reporte de rechazos;").append(r.periodo()).append(";Ventana;").append(r.desde().toLocalDate())
                .append(" a ").append(r.hasta().toLocalDate().minusDays(1)).append('\n');
        sb.append("Promedio de la organizacion (rechazo a viables %);").append(fmt(r.promedioTasaRechazoViables()))
                .append('\n').append('\n');
        sb.append("Manager ID;Decisiones;Aceptadas;Rechazadas;% aceptacion;% rechazo;Candidatos viables;"
                + "Rechazos a viables;% rechazo a viables;Desviacion (puntos);Resaltado;Alertas RN-29\n");
        for (FilaManager f : r.managers()) {
            sb.append(f.managerId()).append(';').append(f.decisiones()).append(';').append(f.aceptadas()).append(';')
                    .append(f.rechazadas()).append(';').append(fmt(f.tasaAceptacion())).append(';')
                    .append(fmt(f.tasaRechazo())).append(';').append(f.candidatosViables()).append(';')
                    .append(f.rechazosViables()).append(';').append(fmt(f.tasaRechazoViables())).append(';')
                    .append(fmt(f.desviacion())).append(';').append(f.resaltado() ? "SI" : "NO").append(';')
                    .append(f.alertasRelacionadas().stream().map(String::valueOf).collect(Collectors.joining(" ")))
                    .append('\n');
        }
        sb.append('\n').append("Detalle de rechazos a candidatos viables (managers resaltados)\n");
        sb.append("Manager ID;Candidato;Puntaje Final;Puntaje del seleccionado;Vacante;Fecha\n");
        r.managers().stream().filter(FilaManager::resaltado).forEach(f -> f.detalle().forEach(d ->
                sb.append(f.managerId()).append(';').append(d.candidato()).append(';').append(fmt(d.puntajeFinal()))
                        .append(';').append(d.puntajeSeleccionado() == null ? "-" : fmt(d.puntajeSeleccionado()))
                        .append(';').append(d.vacante().replace(';', ',')).append(';')
                        .append(d.fecha().toLocalDate()).append('\n')));
        return sb.toString();
    }

    private static double porcentaje(int parte, int total) {
        return total == 0 ? 0 : redondear(100.0 * parte / total);
    }

    private static double redondear(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }
}
