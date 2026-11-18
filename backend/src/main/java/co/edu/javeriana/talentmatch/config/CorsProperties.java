package co.edu.javeriana.talentmatch.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Lee de application.properties la lista de paginas web (origenes)
 * autorizadas a llamar a la API:
 *   talentmatch.cors.origenes-permitidos=http://localhost:5500,http://...
 *
 * Spring separa automaticamente el texto por comas y lo convierte en List<String>.
 */
@ConfigurationProperties(prefix = "talentmatch.cors")
public record CorsProperties(List<String> origenesPermitidos) {

    public CorsProperties {
        // Si no se configuro nada, se usa una lista vacia (nadie externo puede llamar)
        // en lugar de null, para evitar un NullPointerException en CorsConfig.
        // List.copyOf crea una copia que no se puede modificar.
        origenesPermitidos = origenesPermitidos == null ? List.of() : List.copyOf(origenesPermitidos);
    }
}