package co.edu.javeriana.talentmatch.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seccion "costo_rotacion:" del YAML (RN-26, PEN-02): valores aproximados, solo informativos.
 * NO entran al Puntaje Final ni alteran el ranking.
 */
@ConfigurationProperties(prefix = "costo-rotacion")
public record CostoRotacionProperties(Boolean habilitado, Boolean alteraRanking, Salida salida,
                                      RotacionInterna rotacionInterna) {

    public record Salida(Integer recruitingUsd, Integer rampUpUsd, Integer perdidaConocimientoUsd) {

        public int total() {
            return recruitingUsd + rampUpUsd + perdidaConocimientoUsd;
        }
    }

    public record RotacionInterna(Integer capacitacionUsd, Integer transicionUsd, Integer bajaTemporalUsd) {

        public int total() {
            return capacitacionUsd + transicionUsd + bajaTemporalUsd;
        }
    }

    public CostoRotacionProperties {
        Objects.requireNonNull(habilitado, "Falta costo_rotacion.habilitado");
        Objects.requireNonNull(salida, "Falta costo_rotacion.salida");
        Objects.requireNonNull(rotacionInterna, "Falta costo_rotacion.rotacion_interna");
        // PEN-02: el costo de rotacion es informativo. Si alguien intenta usarlo en el ranking, no arranca.
        if (Boolean.TRUE.equals(alteraRanking)) {
            throw new IllegalStateException("costo_rotacion.altera_ranking debe ser false (PEN-02)");
        }
    }
}
