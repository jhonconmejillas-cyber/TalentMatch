package co.edu.javeriana.talentmatch.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seccion "alertas:" del YAML.
 */
@ConfigurationProperties(prefix = "alertas")
public record AlertasProperties(
        // RN-29: cuantos rechazos de candidatos viables (>= 65 %) hace un mismo
        // manager antes de que el sistema alerte a RR.HH. (2)
        Integer rechazosViablesParaAlerta) {

    public AlertasProperties {
        Objects.requireNonNull(rechazosViablesParaAlerta, "Falta alertas.rechazos_viables_para_alerta");
    }
}