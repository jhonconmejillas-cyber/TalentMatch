package co.edu.javeriana.talentmatch.security;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Deja constancia de cada acceso denegado en la tabla bitacora_acceso (RNF-10, HU-006):
 * quien intento, a que recurso y por que. Se escribe directamente con JDBC para que el registro
 * quede aunque la operacion que se intentaba se haya deshecho.
 */
@Component
public class RegistroAccesosDenegados {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroAccesosDenegados.class);
    private static final int MAX = 300;

    private final JdbcTemplate jdbc;

    public RegistroAccesosDenegados(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(String recurso, String motivo) {
        Long usuario = usuarioActual();
        LOG.warn("Acceso denegado: usuario={} recurso={} motivo={}", usuario, recurso, motivo);
        try {
            jdbc.update("INSERT INTO bitacora_acceso (usuario_id, recurso, motivo) VALUES (?, ?, ?)",
                    usuario, recortar(recurso), recortar(motivo));
        } catch (RuntimeException e) {
            // Si la base de datos no esta disponible, el acceso igual se niega; queda al menos en el log.
            LOG.error("No se pudo registrar el acceso denegado en la bitacora", e);
        }
    }

    public List<Map<String, Object>> ultimos(int cantidad) {
        return jdbc.queryForList("""
                SELECT b.id, b.usuario_id AS "usuarioId", u.rol, b.recurso, b.motivo, b.fecha
                FROM bitacora_acceso b LEFT JOIN usuario u ON u.id = b.usuario_id
                ORDER BY b.fecha DESC, b.id DESC LIMIT ?""", cantidad);
    }

    private static Long usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return Long.valueOf(jwt.getSubject());
        }
        return null;
    }

    private static String recortar(String texto) {
        return texto == null ? "" : texto.length() > MAX ? texto.substring(0, MAX) : texto;
    }
}
