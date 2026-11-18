package co.edu.javeriana.talentmatch.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Deja constancia de cada acceso denegado (RNF-10, HU-006): quien intento, a que recurso y por que.
 */
@Component
public class RegistroAccesosDenegados {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroAccesosDenegados.class);

    public void registrar(String recurso, String motivo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = auth == null ? "anonimo" : auth.getName();
        LOG.warn("Acceso denegado: usuario={} recurso={} motivo={}", usuario, recurso, motivo);
    }
}
