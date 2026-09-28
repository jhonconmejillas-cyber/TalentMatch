package co.edu.javeriana.talentmatch.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seccion "reporte_ejecutivo:" del YAML.
 * Ojo: en el prefix se escribe con guion (reporte-ejecutivo) aunque en el YAML
 * este con guion bajo (reporte_ejecutivo); Spring los trata como lo mismo.
 */
@ConfigurationProperties(prefix = "reporte-ejecutivo")
public record ReporteEjecutivoProperties(
        // RN-21: cuantos empleados muestra el reporte para lideres ejecutivos (10)
        Integer topN) {

    public ReporteEjecutivoProperties {
        Objects.requireNonNull(topN, "Falta reporte_ejecutivo.top_n");
    }
}