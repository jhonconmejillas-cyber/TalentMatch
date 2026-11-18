package co.edu.javeriana.talentmatch.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seccion "ranking:" del YAML: limites del indicador de color del ranking (HU-001).
 * Verde >= indicador_verde, amarillo >= indicador_amarillo, rojo por debajo (pero >= umbral).
 */
@ConfigurationProperties(prefix = "ranking")
public record RankingProperties(Integer indicadorVerde, Integer indicadorAmarillo) {

    public RankingProperties {
        Objects.requireNonNull(indicadorVerde, "Falta ranking.indicador_verde");
        Objects.requireNonNull(indicadorAmarillo, "Falta ranking.indicador_amarillo");
        if (indicadorAmarillo > indicadorVerde) {
            throw new IllegalStateException("ranking.indicador_amarillo no puede ser mayor que indicador_verde");
        }
    }
}
