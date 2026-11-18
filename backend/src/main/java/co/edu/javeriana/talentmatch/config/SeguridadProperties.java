package co.edu.javeriana.talentmatch.config;

import java.util.List;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seccion "seguridad:" del YAML (RNF-07).
 */
@ConfigurationProperties(prefix = "seguridad")
public record SeguridadProperties(
        // HU-006: la sesion se cierra tras 30 minutos sin actividad
        Integer minutosInactividadSesion,
        // RF-01: longitud minima de la contrasena al registrarse
        Integer longitudMinimaPassword,
        // RF-01: dominios de correo corporativo aceptados en el registro
        List<String> dominiosCorreoCorporativo) {

    public SeguridadProperties {
        Objects.requireNonNull(minutosInactividadSesion, "Falta seguridad.minutos_inactividad_sesion");
        Objects.requireNonNull(longitudMinimaPassword, "Falta seguridad.longitud_minima_password");
        dominiosCorreoCorporativo = dominiosCorreoCorporativo == null ? List.of()
                : dominiosCorreoCorporativo.stream().map(d -> d.trim().toLowerCase()).toList();
        if (dominiosCorreoCorporativo.isEmpty()) {
            throw new IllegalStateException("Falta seguridad.dominios_correo_corporativo");
        }
    }
}
