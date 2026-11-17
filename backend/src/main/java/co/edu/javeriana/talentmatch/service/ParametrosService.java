package co.edu.javeriana.talentmatch.service;

import co.edu.javeriana.talentmatch.config.AlertasProperties;
import co.edu.javeriana.talentmatch.config.AlgoritmoProperties;
import co.edu.javeriana.talentmatch.config.PlazosProperties;
import co.edu.javeriana.talentmatch.config.ReporteEjecutivoProperties;
import org.springframework.stereotype.Service;

/**
 * Punto unico para que los demas Services lean los parametros de conf/config.yaml (RNF-12).
 *
 * Ningun Service debe tener un 65, un 6 o un 24 escrito en el codigo:
 * siempre se piden a esta clase. Asi, cambiar un umbral solo requiere
 * editar el YAML y reiniciar, sin recompilar.
 */
@Service
public class ParametrosService {

    private final AlgoritmoProperties algoritmo;
    private final PlazosProperties plazos;
    private final AlertasProperties alertas;
    private final ReporteEjecutivoProperties reporte;

    public ParametrosService(AlgoritmoProperties algoritmo, PlazosProperties plazos,
                             AlertasProperties alertas, ReporteEjecutivoProperties reporte) {
        this.algoritmo = algoritmo;
        this.plazos = plazos;
        this.alertas = alertas;
        this.reporte = reporte;
    }

    /** RN-27: Puntaje Final minimo para ser candidato viable (65). */
    public int umbralViabilidad() {
        return algoritmo.umbralViabilidad();
    }

    /** RN-23: peso del % de habilidades avaladas coincidentes (0.7). */
    public double pesoHabilidades() {
        return algoritmo.pesoHabilidades();
    }

    /** RN-23: peso del ajuste por senioridad (0.3). */
    public double pesoSenioridad() {
        return algoritmo.pesoSenioridad();
    }

    /** RN-07: meses minimos en el rol actual para postularse (6). */
    public int antiguedadMinimaRolMeses() {
        return plazos.antiguedadMinimaRolMeses();
    }

    /** RN-10: meses de bloqueo tras un rechazo (6). */
    public int bloqueoRepostulacionMeses() {
        return plazos.bloqueoRepostulacionMeses();
    }

    /** RN-03: horas de activacion diferida tras una rotacion (24). */
    public int activacionDiferidaHoras() {
        return plazos.activacionDiferidaHoras();
    }

    /** RN-12: dias del ciclo de evaluacion del manager (3). */
    public int cicloEvaluacionDias() {
        return plazos.cicloEvaluacionDias();
    }

    /** RN-05: dias sin match antes de alertar (10). */
    public int umbralVacanteSinMatchDias() {
        return plazos.umbralVacanteSinMatchDias();
    }

    /** RN-29: rechazos de candidatos viables antes de alertar a RR.HH. (2). */
    public int rechazosViablesParaAlerta() {
        return alertas.rechazosViablesParaAlerta();
    }

    /** RN-21: empleados del reporte ejecutivo (10). */
    public int topNReporteEjecutivo() {
        return reporte.topN();
    }
}
