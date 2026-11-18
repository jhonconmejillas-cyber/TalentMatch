package co.edu.javeriana.talentmatch.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Seccion "rendimiento:" del YAML: metas de RNF-04 / HU-005. */
@ConfigurationProperties(prefix = "rendimiento")
public record RendimientoProperties(Integer maxSegundosRespuesta, Integer maxSegundosRanking,
                                    Integer maxUsuariosConcurrentes, Integer milisegundosIndicadorCarga) {

    public RendimientoProperties {
        Objects.requireNonNull(maxSegundosRespuesta, "Falta rendimiento.max_segundos_respuesta");
        Objects.requireNonNull(maxSegundosRanking, "Falta rendimiento.max_segundos_ranking");
        Objects.requireNonNull(maxUsuariosConcurrentes, "Falta rendimiento.max_usuarios_concurrentes");
        if (milisegundosIndicadorCarga == null) {
            milisegundosIndicadorCarga = 300;
        }
    }
}
