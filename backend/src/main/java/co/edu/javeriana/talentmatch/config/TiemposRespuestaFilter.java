package co.edu.javeriana.talentmatch.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registra el tiempo de respuesta de cada peticion a la API en el log de la aplicacion
 * (HU-005, subtarea 6). Si supera la meta de RNF-04 se registra como advertencia.
 * Formato: "tiempo-respuesta GET /api/v1/vacantes/1/ranking 200 35 ms"
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TiemposRespuestaFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger("tiempos-respuesta");

    private final RendimientoProperties rendimiento;

    public TiemposRespuestaFilter(RendimientoProperties rendimiento) {
        this.rendimiento = rendimiento;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        long inicio = System.nanoTime();
        try {
            cadena.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            long meta = (request.getRequestURI().endsWith("/ranking") ? rendimiento.maxSegundosRanking()
                    : rendimiento.maxSegundosRespuesta()) * 1000L;
            String linea = "tiempo-respuesta {} {} {} {} ms";
            if (ms > meta) {
                LOG.warn(linea + " (supera la meta de {} ms)", request.getMethod(), request.getRequestURI(),
                        response.getStatus(), ms, meta);
            } else {
                LOG.info(linea, request.getMethod(), request.getRequestURI(), response.getStatus(), ms);
            }
        }
    }
}
