package co.edu.javeriana.talentmatch.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Seccion "reporte_rechazos:" del YAML (HU-002). */
@ConfigurationProperties(prefix = "reporte-rechazos")
public record ReporteRechazosProperties(Integer mesesVentana, Integer desviacionResaltadoPct) {

    public ReporteRechazosProperties {
        Objects.requireNonNull(mesesVentana, "Falta reporte_rechazos.meses_ventana");
        Objects.requireNonNull(desviacionResaltadoPct, "Falta reporte_rechazos.desviacion_resaltado_pct");
        if (mesesVentana < 1) {
            throw new IllegalStateException("reporte_rechazos.meses_ventana debe ser al menos 1");
        }
    }
}
