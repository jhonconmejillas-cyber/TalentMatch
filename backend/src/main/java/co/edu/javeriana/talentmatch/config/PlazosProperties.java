package co.edu.javeriana.talentmatch.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Plazos de las reglas de negocio, leidos de la seccion "plazos:" del YAML.
 * Cambiarlos solo requiere editar conf/config.yaml y reiniciar (RNF-12).
 */
@ConfigurationProperties(prefix = "plazos")
public record PlazosProperties(
        Integer antiguedadMinimaRolMeses,   // RN-07: meses minimos en el rol para postularse (6)
        Integer bloqueoRepostulacionMeses,  // RN-10: meses de espera tras un rechazo (6)
        Integer activacionDiferidaHoras,    // RN-03: horas para activar la vacante tras una rotacion (24)
        Integer cicloEvaluacionDias,        // RN-12: cada cuantos dias el manager resuelve (3)
        Integer umbralVacanteSinMatchDias)  // RN-05: dias sin match antes de alertar (10)
{

    // Si falta cualquier plazo, la aplicacion no arranca y dice cual falta.
    public PlazosProperties {
        Objects.requireNonNull(antiguedadMinimaRolMeses, "Falta plazos.antiguedad_minima_rol_meses");
        Objects.requireNonNull(bloqueoRepostulacionMeses, "Falta plazos.bloqueo_repostulacion_meses");
        Objects.requireNonNull(activacionDiferidaHoras, "Falta plazos.activacion_diferida_horas");
        Objects.requireNonNull(cicloEvaluacionDias, "Falta plazos.ciclo_evaluacion_dias");
        Objects.requireNonNull(umbralVacanteSinMatchDias, "Falta plazos.umbral_vacante_sin_match_dias");
    }
}