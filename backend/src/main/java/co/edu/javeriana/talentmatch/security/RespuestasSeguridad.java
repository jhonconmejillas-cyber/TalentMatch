package co.edu.javeriana.talentmatch.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Respuestas JSON en espanol (RNF-13) cuando falta la sesion (401) o el rol
 * no tiene permiso (403). Sin esto, Spring respondia con un cuerpo vacio.
 */
@Component
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final RegistroAccesosDenegados accesosDenegados;

    public RespuestasSeguridad(RegistroAccesosDenegados accesosDenegados) {
        this.accesosDenegados = accesosDenegados;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        escribir(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Tu sesión no es válida o expiró. Inicia sesión de nuevo.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        accesosDenegados.registrar(request.getMethod() + " " + request.getRequestURI(), "Rol sin permiso");
        escribir(response, HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para realizar esta acción.");
    }

    private static void escribir(HttpServletResponse response, int estado, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
