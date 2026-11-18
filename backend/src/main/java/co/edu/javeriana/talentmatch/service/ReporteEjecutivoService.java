package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.model.AsignacionProyecto;
import co.edu.javeriana.talentmatch.model.Empleado;
import co.edu.javeriana.talentmatch.model.MotivoFinAsignacion;
import co.edu.javeriana.talentmatch.repository.EmpleadoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reporte ejecutivo top 10 (RN-21, RF-29, CU_22, HU-010): los empleados con mas habilidades
 * AVALADAS (RN-20), luego mas rotaciones y luego mas anios en la empresa.
 * Es solo un insumo: la decision de ascenso es humana y fuera de la app.
 */
@Service
public class ReporteEjecutivoService {

    /** RN-22: mensaje de incentivo que se muestra al empleado en cada rotacion confirmada. */
    public static final String MENSAJE_INCENTIVO = "¡Felicitaciones por tu rotación! Tus rotaciones y las habilidades "
            + "que te avalen aumentan tu probabilidad de aparecer en el reporte ejecutivo de los 10 empleados más destacados.";

    public static final String AVISO_INSUMO = "Este reporte es un insumo de apoyo para las conversaciones de ascenso. "
            + "La aplicación no ejecuta ni registra decisiones de ascenso: la decisión final es humana.";

    private final EmpleadoRepository empleados;
    private final ParametrosService parametros;
    private final Clock reloj;

    public ReporteEjecutivoService(EmpleadoRepository empleados, ParametrosService parametros, Clock reloj) {
        this.empleados = empleados;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    public record FilaTop(int posicion, Long empleadoId, String nombre, String area, int habilidadesAvaladas,
                          int rotaciones, int aniosEnLaEmpresa) {
    }

    public record ReporteTop(int topN, String aviso, List<FilaTop> empleados) {
    }

    @Transactional(readOnly = true)
    public ReporteTop top() {
        LocalDate hoy = LocalDate.now(reloj);
        List<Medida> orden = empleados.findByActivoTrue().stream()
                .map(e -> new Medida(e, e.habilidadesAvaladas().size(), rotaciones(e), anios(e, hoy)))
                .sorted(Comparator.comparingInt(Medida::habilidades).reversed()
                        .thenComparing(Comparator.comparingInt(Medida::rotaciones).reversed())
                        .thenComparing(Comparator.comparingInt(Medida::anios).reversed())
                        .thenComparing(m -> m.empleado().getId()))
                .limit(parametros.topNReporteEjecutivo())
                .toList();
        List<FilaTop> filas = new java.util.ArrayList<>();
        for (int i = 0; i < orden.size(); i++) {
            Medida m = orden.get(i);
            filas.add(new FilaTop(i + 1, m.empleado().getId(), m.empleado().getNombre(), m.empleado().getArea(),
                    m.habilidades(), m.rotaciones(), m.anios()));
        }
        return new ReporteTop(parametros.topNReporteEjecutivo(), AVISO_INSUMO, filas);
    }

    private record Medida(Empleado empleado, int habilidades, int rotaciones, int anios) {
    }

    private static int rotaciones(Empleado e) {
        return (int) e.getAsignaciones().stream()
                .map(AsignacionProyecto::getMotivoFin)
                .filter(m -> m == MotivoFinAsignacion.ROTACION_ACEPTADA).count();
    }

    private static int anios(Empleado e, LocalDate hoy) {
        return e.getFechaIngresoEmpresa() == null ? 0 : Period.between(e.getFechaIngresoEmpresa(), hoy).getYears();
    }
}
